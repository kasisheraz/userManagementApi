package com.fincore.usermgmt.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fincore.usermgmt.dto.*;
import com.fincore.usermgmt.service.OrganisationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Edge case tests for OrganisationController
 * Covers validation failures, error scenarios, business rule violations, and authorization failures
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OrganisationControllerEdgeCaseTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrganisationService organisationService;

    // ===== Validation Tests =====

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void createOrganisation_withNullLegalName_shouldReturnBadRequest() throws Exception {
        OrganisationCreateDTO createDTO = OrganisationCreateDTO.builder()
                .ownerId(1L)
                .organisationType("LTD")
                .legalName(null)  // Violates @NotBlank
                .registrationNumber("12345678")
                .build();

        mockMvc.perform(post("/api/organisations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void createOrganisation_withEmptyLegalName_shouldReturnBadRequest() throws Exception {
        OrganisationCreateDTO createDTO = OrganisationCreateDTO.builder()
                .ownerId(1L)
                .organisationType("LTD")
                .legalName("")  // Violates @NotBlank
                .registrationNumber("12345678")
                .build();

        mockMvc.perform(post("/api/organisations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void createOrganisation_withNullOwnerId_shouldReturnBadRequest() throws Exception {
        OrganisationCreateDTO createDTO = OrganisationCreateDTO.builder()
                .ownerId(null)  // Violates @NotNull
                .organisationType("LTD")
                .legalName("Test Company Ltd")
                .registrationNumber("12345678")
                .build();

        mockMvc.perform(post("/api/organisations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void createOrganisation_withNullOrganisationType_shouldReturnBadRequest() throws Exception {
        OrganisationCreateDTO createDTO = OrganisationCreateDTO.builder()
                .ownerId(1L)
                .organisationType(null)  // Violates @NotBlank
                .legalName("Test Company Ltd")
                .registrationNumber("12345678")
                .build();

        mockMvc.perform(post("/api/organisations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isBadRequest());
    }

    // ===== Retrieval Edge Cases =====

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void getOrganisation_withNonExistentId_shouldReturnNotFound() throws Exception {
        when(organisationService.getOrganisationById(999L))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/organisations/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void getOrganisation_withZeroId_shouldReturnNotFound() throws Exception {
        when(organisationService.getOrganisationById(0L))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/organisations/0"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void getOrganisationsByStatus_withInvalidStatus_shouldReturnBadRequest() throws Exception {
        when(organisationService.getOrganisationsByStatus("INVALID_STATUS"))
                .thenThrow(new IllegalArgumentException("Invalid status: INVALID_STATUS"));

        mockMvc.perform(get("/api/organisations/status/INVALID_STATUS"))
                .andExpect(status().isBadRequest());
    }

    // ===== Update Edge Cases =====

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void updateOrganisation_withNonExistentId_shouldReturnNotFound() throws Exception {
        OrganisationUpdateDTO updateDTO = OrganisationUpdateDTO.builder()
                .legalName("Updated Company Ltd")
                .businessName("Updated Business")
                .build();

        when(organisationService.updateOrganisation(eq(999L), any(OrganisationUpdateDTO.class)))
                .thenThrow(new RuntimeException("Organisation not found with ID: 999"));

        mockMvc.perform(put("/api/organisations/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void updateOrganisationStatus_withNonExistentId_shouldReturnNotFound() throws Exception {
        when(organisationService.updateOrganisationStatus(eq(999L), eq("APPROVED"), anyString()))
                .thenThrow(new RuntimeException("Organisation not found with ID: 999"));

        mockMvc.perform(patch("/api/organisations/999/status")
                        .param("status", "APPROVED")
                        .param("reason", "Test reason"))
                .andExpect(status().isNotFound());
    }

    // ===== Delete Edge Cases =====

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void deleteOrganisation_withNonExistentId_shouldReturnNotFound() throws Exception {
        doThrow(new RuntimeException("Organisation not found with ID: 999"))
                .when(organisationService).deleteOrganisation(999L);

        mockMvc.perform(delete("/api/organisations/999"))
                .andExpect(status().isNotFound());
    }

    // ===== Malformed Request Tests =====

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void createOrganisation_withMalformedJson_shouldReturnInternalServerError() throws Exception {
        mockMvc.perform(post("/api/organisations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"legalName\": }"))  // Invalid JSON
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void updateOrganisation_withMalformedJson_shouldReturnInternalServerError() throws Exception {
        mockMvc.perform(put("/api/organisations/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"legalName\": \"Test\",}"))  // Invalid JSON (trailing comma)
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void searchOrganisations_withMalformedJson_shouldReturnInternalServerError() throws Exception {
        mockMvc.perform(post("/api/organisations/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"searchTerm\":"))  // Invalid JSON
                .andExpect(status().isInternalServerError());
    }

    // ===== Authorization Tests =====

    @Test
    void createOrganisation_withoutAuthentication_shouldReturnForbidden() throws Exception {
        OrganisationCreateDTO createDTO = OrganisationCreateDTO.builder()
                .ownerId(1L)
                .organisationType("LTD")
                .legalName("Test Company Ltd")
                .registrationNumber("12345678")
                .build();

        mockMvc.perform(post("/api/organisations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isForbidden());
    }

    @Test
    void getOrganisation_withoutAuthentication_shouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/organisations/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAllOrganisations_withoutAuthentication_shouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/organisations"))
                .andExpect(status().isForbidden());
    }

    @Test
    void searchOrganisations_withoutAuthentication_shouldReturnForbidden() throws Exception {
        OrganisationSearchDTO searchDTO = OrganisationSearchDTO.builder()
                .searchTerm("Test")
                .build();

        mockMvc.perform(post("/api/organisations/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(searchDTO)))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateOrganisation_withoutAuthentication_shouldReturnForbidden() throws Exception {
        OrganisationUpdateDTO updateDTO = OrganisationUpdateDTO.builder()
                .legalName("Updated Company Ltd")
                .build();

        mockMvc.perform(put("/api/organisations/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateOrganisationStatus_withoutAuthentication_shouldReturnForbidden() throws Exception {
        mockMvc.perform(patch("/api/organisations/1/status")
                        .param("status", "APPROVED"))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteOrganisation_withoutAuthentication_shouldReturnForbidden() throws Exception {
        mockMvc.perform(delete("/api/organisations/1"))
                .andExpect(status().isForbidden());
    }
}

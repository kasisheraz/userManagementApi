package com.fincore.usermgmt.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fincore.usermgmt.dto.*;
import com.fincore.usermgmt.service.OrganisationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OrganisationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrganisationService organisationService;

    private OrganisationDTO organisationDTO;
    private OrganisationCreateDTO createDTO;

    @BeforeEach
    void setUp() {
        organisationDTO = OrganisationDTO.builder()
                .id(1L)
                .ownerId(1L)
                .ownerName("John Doe")
                .legalName("Test Company Ltd")
                .businessName("Test Business")
                .organisationType("LTD")
                .status("PENDING")
                .registrationNumber("12345678")
                .companyNumber("CN12345")
                .countryOfIncorporation("United Kingdom")
                .build();

        createDTO = OrganisationCreateDTO.builder()
                .ownerId(1L)
                .legalName("Test Company Ltd")
                .businessName("Test Business")
                .organisationType("LTD")
                .registrationNumber("12345678")
                .companyNumber("CN12345")
                .countryOfIncorporation("United Kingdom")
                .build();
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void createOrganisation_Success() throws Exception {
        when(organisationService.createOrganisation(any(OrganisationCreateDTO.class)))
                .thenReturn(organisationDTO);

        mockMvc.perform(post("/api/organisations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.legalName").value("Test Company Ltd"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void getOrganisationById_Found() throws Exception {
        when(organisationService.getOrganisationById(1L)).thenReturn(Optional.of(organisationDTO));

        mockMvc.perform(get("/api/organisations/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.legalName").value("Test Company Ltd"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void getOrganisationById_NotFound() throws Exception {
        when(organisationService.getOrganisationById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/organisations/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void getAllOrganisations_Success() throws Exception {
        PagedResponse<OrganisationDTO> pagedResponse = PagedResponse.<OrganisationDTO>builder()
                .content(Arrays.asList(organisationDTO))
                .page(0)
                .size(20)
                .totalElements(1)
                .totalPages(1)
                .first(true)
                .last(true)
                .build();

        when(organisationService.getAllOrganisations(anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(pagedResponse);

        mockMvc.perform(get("/api/organisations")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void getOrganisationsByOwner_Success() throws Exception {
        List<OrganisationDTO> organisations = Arrays.asList(organisationDTO);
        when(organisationService.getOrganisationsByOwner(1L)).thenReturn(organisations);

        mockMvc.perform(get("/api/organisations/owner/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].legalName").value("Test Company Ltd"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void getOrganisationsByStatus_Success() throws Exception {
        List<OrganisationDTO> organisations = Arrays.asList(organisationDTO);
        when(organisationService.getOrganisationsByStatus("PENDING")).thenReturn(organisations);

        mockMvc.perform(get("/api/organisations/status/PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("PENDING"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void updateOrganisation_Success() throws Exception {
        OrganisationUpdateDTO updateDTO = OrganisationUpdateDTO.builder()
                .businessName("Updated Business Name")
                .build();

        when(organisationService.updateOrganisation(eq(1L), any(OrganisationUpdateDTO.class)))
                .thenReturn(organisationDTO);

        mockMvc.perform(put("/api/organisations/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void updateOrganisation_NotFound() throws Exception {
        OrganisationUpdateDTO updateDTO = OrganisationUpdateDTO.builder()
                .businessName("Updated Business Name")
                .build();

        when(organisationService.updateOrganisation(eq(1L), any(OrganisationUpdateDTO.class)))
                .thenThrow(new RuntimeException("Organisation not found"));

        mockMvc.perform(put("/api/organisations/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void updateOrganisationStatus_Success() throws Exception {
        when(organisationService.updateOrganisationStatus(eq(1L), eq("ACTIVE"), anyString()))
                .thenReturn(organisationDTO);

        mockMvc.perform(patch("/api/organisations/1/status")
                        .param("status", "ACTIVE")
                        .param("reason", "Approved"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void deleteOrganisation_Success() throws Exception {
        mockMvc.perform(delete("/api/organisations/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void checkRegistrationNumberExists_True() throws Exception {
        when(organisationService.existsByRegistrationNumber("12345678")).thenReturn(true);

        mockMvc.perform(get("/api/organisations/exists/registration/12345678"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void checkRegistrationNumberExists_False() throws Exception {
        when(organisationService.existsByRegistrationNumber("99999999")).thenReturn(false);

        mockMvc.perform(get("/api/organisations/exists/registration/99999999"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    @Test
    void createOrganisation_Unauthorized() throws Exception {
        mockMvc.perform(post("/api/organisations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isForbidden());
    }
}

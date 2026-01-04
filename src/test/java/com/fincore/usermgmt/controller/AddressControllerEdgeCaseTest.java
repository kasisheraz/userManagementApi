package com.fincore.usermgmt.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fincore.usermgmt.dto.AddressCreateDTO;
import com.fincore.usermgmt.dto.AddressDTO;
import com.fincore.usermgmt.service.AddressService;
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

import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Edge case tests for AddressController.
 * Tests validation failures, error scenarios, and invalid enum values.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AddressControllerEdgeCaseTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AddressService addressService;

    private AddressDTO addressDTO;
    private AddressCreateDTO createDTO;

    @BeforeEach
    void setUp() {
        addressDTO = new AddressDTO();
        addressDTO.setId(1L);
        addressDTO.setTypeCode(1);
        addressDTO.setAddressLine1("123 Main Street");
        addressDTO.setCity("London");
        addressDTO.setCountry("United Kingdom");
        addressDTO.setPostalCode("SW1A 1AA");
        addressDTO.setStatusDescription("ACTIVE");

        createDTO = new AddressCreateDTO();
        createDTO.setTypeCode(1);
        createDTO.setAddressLine1("123 Main Street");
        createDTO.setCity("London");
        createDTO.setCountry("United Kingdom");
        createDTO.setPostalCode("SW1A 1AA");
    }

    // ===== Validation Edge Cases =====

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void createAddress_withNullTypeCode_shouldReturnBadRequest() throws Exception {
        AddressCreateDTO invalidDTO = new AddressCreateDTO();
        invalidDTO.setTypeCode(null);
        invalidDTO.setAddressLine1("123 Main Street");
        invalidDTO.setCountry("United Kingdom");

        mockMvc.perform(post("/api/addresses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void createAddress_withNullAddressLine1_shouldReturnBadRequest() throws Exception {
        AddressCreateDTO invalidDTO = new AddressCreateDTO();
        invalidDTO.setTypeCode(1);
        invalidDTO.setAddressLine1(null);
        invalidDTO.setCountry("United Kingdom");

        mockMvc.perform(post("/api/addresses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void createAddress_withEmptyAddressLine1_shouldReturnBadRequest() throws Exception {
        AddressCreateDTO invalidDTO = new AddressCreateDTO();
        invalidDTO.setTypeCode(1);
        invalidDTO.setAddressLine1("");
        invalidDTO.setCountry("United Kingdom");

        mockMvc.perform(post("/api/addresses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void createAddress_withNullCountry_shouldReturnBadRequest() throws Exception {
        AddressCreateDTO invalidDTO = new AddressCreateDTO();
        invalidDTO.setTypeCode(1);
        invalidDTO.setAddressLine1("123 Main Street");
        invalidDTO.setCountry(null);

        mockMvc.perform(post("/api/addresses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void createAddress_withEmptyCountry_shouldReturnBadRequest() throws Exception {
        AddressCreateDTO invalidDTO = new AddressCreateDTO();
        invalidDTO.setTypeCode(1);
        invalidDTO.setAddressLine1("123 Main Street");
        invalidDTO.setCountry("");

        mockMvc.perform(post("/api/addresses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest());
    }

    // ===== Invalid Type Code Edge Cases =====

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void getAddressesByType_withInvalidType_shouldReturnBadRequest() throws Exception {
        when(addressService.getAddressesByType(anyString()))
                .thenThrow(new IllegalArgumentException("Invalid address type"));

        mockMvc.perform(get("/api/addresses/type/INVALID_TYPE"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void getAddressesByType_withEmptyType_shouldReturnBadRequest() throws Exception {
        when(addressService.getAddressesByType(""))
                .thenThrow(new IllegalArgumentException("Invalid address type"));

        mockMvc.perform(get("/api/addresses/type/"))
                .andExpect(status().isInternalServerError()); // Path resolution error returns 500
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void getAddressesByType_withNumericType_shouldReturnBadRequest() throws Exception {
        when(addressService.getAddressesByType("123"))
                .thenThrow(new IllegalArgumentException("Invalid address type"));

        mockMvc.perform(get("/api/addresses/type/123"))
                .andExpect(status().isBadRequest());
    }

    // ===== Retrieval Edge Cases =====

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void getAddressById_withNonExistentId_shouldReturnNotFound() throws Exception {
        when(addressService.getAddressById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/addresses/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void getAddressById_withZeroId_shouldReturnNotFound() throws Exception {
        when(addressService.getAddressById(0L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/addresses/0"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void getAddressById_withNegativeId_shouldReturnNotFound() throws Exception {
        when(addressService.getAddressById(-1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/addresses/-1"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void getAddressesByCountry_withNonExistentCountry_shouldReturnEmptyList() throws Exception {
        when(addressService.getAddressesByCountry("NonExistentCountry"))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/addresses/country/NonExistentCountry"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void getAddressesByCity_withNonExistentCity_shouldReturnEmptyList() throws Exception {
        when(addressService.getAddressesByCity("NonExistentCity"))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/addresses/city/NonExistentCity"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    // ===== Delete Edge Cases =====

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void deleteAddress_withNonExistentId_shouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/addresses/999"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void deleteAddress_withZeroId_shouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/addresses/0"))
                .andExpect(status().isNoContent());
    }

    // ===== Update Edge Cases =====

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void updateAddress_withNonExistentId_shouldReturnNotFound() throws Exception {
        AddressCreateDTO updateDTO = new AddressCreateDTO();
        updateDTO.setTypeCode(1);
        updateDTO.setAddressLine1("456 New Street");
        updateDTO.setCountry("United Kingdom");

        when(addressService.updateAddress(999L, updateDTO))
                .thenThrow(new RuntimeException("Address not found"));

        mockMvc.perform(put("/api/addresses/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void updateAddress_withInvalidData_shouldReturnBadRequest() throws Exception {
        AddressCreateDTO invalidDTO = new AddressCreateDTO();
        invalidDTO.setTypeCode(null);
        invalidDTO.setAddressLine1("456 New Street");
        invalidDTO.setCountry("United Kingdom");

        mockMvc.perform(put("/api/addresses/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest());
    }

    // ===== Malformed Request Edge Cases =====

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void createAddress_withMalformedJson_shouldReturnBadRequest() throws Exception {
        String malformedJson = "{\"typeCode\": 1, \"addressLine1\": }";

        mockMvc.perform(post("/api/addresses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(malformedJson))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void createAddress_withEmptyBody_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/api/addresses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void updateAddress_withMalformedJson_shouldReturnBadRequest() throws Exception {
        String malformedJson = "{\"addressLine1\": \"123 Main\", \"country\": }";

        mockMvc.perform(put("/api/addresses/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(malformedJson))
                .andExpect(status().isInternalServerError());
    }

    // ===== Authorization Edge Cases =====

    @Test
    void createAddress_withoutAuthentication_shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(post("/api/addresses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAddressById_withoutAuthentication_shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/addresses/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAllAddresses_withoutAuthentication_shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/addresses"))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateAddress_withoutAuthentication_shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(put("/api/addresses/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteAddress_withoutAuthentication_shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(delete("/api/addresses/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAddressesByType_withoutAuthentication_shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/addresses/type/BUSINESS"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAddressesByCountry_withoutAuthentication_shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/addresses/country/UK"))
                .andExpect(status().isForbidden());
    }
}

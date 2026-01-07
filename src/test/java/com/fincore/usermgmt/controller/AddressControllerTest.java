package com.fincore.usermgmt.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fincore.usermgmt.dto.AddressCreateDTO;
import com.fincore.usermgmt.dto.AddressDTO;
import com.fincore.usermgmt.service.AddressService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AddressController.class)
@AutoConfigureMockMvc(addFilters = false)
class AddressControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AddressService addressService;

    @Test
    void createAddress_WithValidData_ShouldReturnCreated() throws Exception {
        // Given
        AddressCreateDTO createDTO = new AddressCreateDTO();
        createDTO.setTypeCode(3); // REGISTERED
        createDTO.setAddressLine1("123 Main Street");
        createDTO.setPostalCode("EC1A 1BB");
        createDTO.setCity("London");
        createDTO.setCountry("United Kingdom");

        AddressDTO addressDTO = new AddressDTO();
        addressDTO.setId(1L);
        addressDTO.setAddressType("REGISTERED");
        addressDTO.setAddressLine1("123 Main Street");
        addressDTO.setPostalCode("EC1A 1BB");
        addressDTO.setCity("London");
        addressDTO.setCountry("United Kingdom");

        when(addressService.createAddress(any(AddressCreateDTO.class))).thenReturn(addressDTO);

        // When & Then
        mockMvc.perform(post("/api/addresses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.addressType").value("REGISTERED"))
                .andExpect(jsonPath("$.addressLine1").value("123 Main Street"))
                .andExpect(jsonPath("$.postalCode").value("EC1A 1BB"))
                .andExpect(jsonPath("$.city").value("London"))
                .andExpect(jsonPath("$.country").value("United Kingdom"));
    }

    @Test
    void getAddressById_WhenExists_ShouldReturnAddress() throws Exception {
        // Given
        AddressDTO addressDTO = new AddressDTO();
        addressDTO.setId(1L);
        addressDTO.setAddressType("REGISTERED");
        addressDTO.setAddressLine1("123 Main Street");
        addressDTO.setCity("London");

        when(addressService.getAddressById(1L)).thenReturn(Optional.of(addressDTO));

        // When & Then
        mockMvc.perform(get("/api/addresses/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.addressType").value("REGISTERED"))
                .andExpect(jsonPath("$.city").value("London"));
    }

    @Test
    void getAddressById_WhenNotExists_ShouldReturn404() throws Exception {
        // Given
        when(addressService.getAddressById(999L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/addresses/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllAddresses_ShouldReturnList() throws Exception {
        // Given
        AddressDTO address1 = new AddressDTO();
        address1.setId(1L);
        address1.setAddressType("REGISTERED");
        address1.setCity("London");

        AddressDTO address2 = new AddressDTO();
        address2.setId(2L);
        address2.setAddressType("POSTAL");
        address2.setCity("Manchester");

        List<AddressDTO> addresses = Arrays.asList(address1, address2);
        when(addressService.getAllAddresses()).thenReturn(addresses);

        // When & Then
        mockMvc.perform(get("/api/addresses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].city").value("London"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].city").value("Manchester"));
    }

    @Test
    void getAllAddresses_WhenEmpty_ShouldReturnEmptyList() throws Exception {
        // Given
        when(addressService.getAllAddresses()).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/api/addresses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getAddressesByType_WithValidType_ShouldReturnList() throws Exception {
        // Given
        AddressDTO address1 = new AddressDTO();
        address1.setId(1L);
        address1.setAddressType("REGISTERED");

        when(addressService.getAddressesByType("REGISTERED")).thenReturn(Collections.singletonList(address1));

        // When & Then
        mockMvc.perform(get("/api/addresses/type/REGISTERED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].addressType").value("REGISTERED"));
    }

    @Test
    void getAddressesByType_WithInvalidType_ShouldReturnBadRequest() throws Exception {
        // Given
        when(addressService.getAddressesByType("INVALID_TYPE"))
                .thenThrow(new IllegalArgumentException("Invalid address type"));

        // When & Then
        mockMvc.perform(get("/api/addresses/type/INVALID_TYPE"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAddressesByCountry_ShouldReturnList() throws Exception {
        // Given
        AddressDTO address1 = new AddressDTO();
        address1.setId(1L);
        address1.setCountry("United Kingdom");
        address1.setCity("London");

        AddressDTO address2 = new AddressDTO();
        address2.setId(2L);
        address2.setCountry("United Kingdom");
        address2.setCity("Manchester");

        when(addressService.getAddressesByCountry("United Kingdom"))
                .thenReturn(Arrays.asList(address1, address2));

        // When & Then
        mockMvc.perform(get("/api/addresses/country/United Kingdom"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].country").value("United Kingdom"))
                .andExpect(jsonPath("$[1].country").value("United Kingdom"));
    }

    @Test
    void getAddressesByCity_ShouldReturnList() throws Exception {
        // Given
        AddressDTO address1 = new AddressDTO();
        address1.setId(1L);
        address1.setCity("London");

        when(addressService.getAddressesByCity("London"))
                .thenReturn(Collections.singletonList(address1));

        // When & Then
        mockMvc.perform(get("/api/addresses/city/London"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].city").value("London"));
    }

    @Test
    void updateAddress_WhenExists_ShouldReturnUpdated() throws Exception {
        // Given
        AddressCreateDTO updateDTO = new AddressCreateDTO();
        updateDTO.setTypeCode(3);
        updateDTO.setAddressLine1("456 New Street");
        updateDTO.setCity("London");

        AddressDTO updatedDTO = new AddressDTO();
        updatedDTO.setId(1L);
        updatedDTO.setAddressLine1("456 New Street");
        updatedDTO.setCity("London");

        when(addressService.updateAddress(eq(1L), any(AddressCreateDTO.class)))
                .thenReturn(Optional.of(updatedDTO));

        // When & Then
        mockMvc.perform(put("/api/addresses/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.addressLine1").value("456 New Street"));
    }

    @Test
    void updateAddress_WhenNotExists_ShouldReturn404() throws Exception {
        // Given
        AddressCreateDTO updateDTO = new AddressCreateDTO();
        updateDTO.setTypeCode(3);
        updateDTO.setAddressLine1("456 New Street");

        when(addressService.updateAddress(eq(999L), any(AddressCreateDTO.class)))
                .thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(put("/api/addresses/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteAddress_WhenExists_ShouldReturnNoContent() throws Exception {
        // Given
        when(addressService.deleteAddress(1L)).thenReturn(true);

        // When & Then
        mockMvc.perform(delete("/api/addresses/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteAddress_WhenNotExists_ShouldReturn404() throws Exception {
        // Given
        when(addressService.deleteAddress(999L)).thenReturn(false);

        // When & Then
        mockMvc.perform(delete("/api/addresses/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createAddress_WithMissingRequiredFields_ShouldReturnBadRequest() throws Exception {
        // Given - empty DTO missing required fields
        AddressCreateDTO createDTO = new AddressCreateDTO();

        // When & Then
        mockMvc.perform(post("/api/addresses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isBadRequest());
    }
}

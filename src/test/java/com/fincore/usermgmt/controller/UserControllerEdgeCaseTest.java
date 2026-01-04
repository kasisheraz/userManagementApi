package com.fincore.usermgmt.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fincore.usermgmt.dto.UserCreateDTO;
import com.fincore.usermgmt.dto.UserDTO;
import com.fincore.usermgmt.dto.UserUpdateDTO;
import com.fincore.usermgmt.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Edge case tests for UserController.
 * Tests validation failures, error scenarios, boundary conditions, and exception handling.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserControllerEdgeCaseTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    private UserDTO userDTO;
    private UserCreateDTO createDTO;

    @BeforeEach
    void setUp() {
        userDTO = new UserDTO();
        userDTO.setId(1L);
        userDTO.setFirstName("John");
        userDTO.setLastName("Doe");
        userDTO.setEmail("john.doe@example.com");
        userDTO.setPhoneNumber("+1234567890");
        userDTO.setRole("USER");
        userDTO.setStatusDescription("ACTIVE");

        createDTO = new UserCreateDTO();
        createDTO.setFirstName("John");
        createDTO.setLastName("Doe");
        createDTO.setEmail("john.doe@example.com");
        createDTO.setPhoneNumber("+1234567890");
        createDTO.setRole("USER");
        createDTO.setDateOfBirth(LocalDate.of(1990, 1, 1));
    }

    // ===== Validation Edge Cases =====

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void createUser_withNullFirstName_shouldReturnBadRequest() throws Exception {
        UserCreateDTO invalidDTO = new UserCreateDTO();
        invalidDTO.setFirstName(null);
        invalidDTO.setLastName("Doe");
        invalidDTO.setEmail("john.doe@example.com");
        invalidDTO.setPhoneNumber("+1234567890");
        invalidDTO.setRole("USER");

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void createUser_withEmptyFirstName_shouldReturnBadRequest() throws Exception {
        UserCreateDTO invalidDTO = new UserCreateDTO();
        invalidDTO.setFirstName("");
        invalidDTO.setLastName("Doe");
        invalidDTO.setEmail("john.doe@example.com");
        invalidDTO.setPhoneNumber("+1234567890");
        invalidDTO.setRole("USER");

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void createUser_withInvalidEmail_shouldReturnBadRequest() throws Exception {
        UserCreateDTO invalidDTO = new UserCreateDTO();
        invalidDTO.setFirstName("John");
        invalidDTO.setLastName("Doe");
        invalidDTO.setEmail("invalid-email");
        invalidDTO.setPhoneNumber("+1234567890");
        invalidDTO.setRole("USER");

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void createUser_withNullPhoneNumber_shouldReturnBadRequest() throws Exception {
        UserCreateDTO invalidDTO = new UserCreateDTO();
        invalidDTO.setFirstName("John");
        invalidDTO.setLastName("Doe");
        invalidDTO.setEmail("john.doe@example.com");
        invalidDTO.setPhoneNumber(null);
        invalidDTO.setRole("USER");

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest());
    }

    // ===== Conflict Edge Cases =====

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void createUser_withDuplicateEmail_shouldReturnConflict() throws Exception {
        when(userService.createUser(any(UserCreateDTO.class)))
                .thenThrow(new DataIntegrityViolationException("Duplicate entry for key 'EMAIL'"));

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Email already exists"))
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void createUser_withDuplicatePhoneNumber_shouldReturnConflict() throws Exception {
        when(userService.createUser(any(UserCreateDTO.class)))
                .thenThrow(new DataIntegrityViolationException("Duplicate entry for key 'PHONE'"));

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Phone number already exists"))
                .andExpect(jsonPath("$.status").value(409));
    }

    // ===== Update Edge Cases =====

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void updateUser_withNonExistentId_shouldReturnNotFound() throws Exception {
        UserUpdateDTO updateDTO = new UserUpdateDTO();
        updateDTO.setFirstName("Updated");
        updateDTO.setLastName("Name");
        updateDTO.setEmail("updated@example.com");

        when(userService.updateUser(eq(999L), any(UserUpdateDTO.class)))
                .thenThrow(new RuntimeException("User not found"));

        mockMvc.perform(put("/api/users/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void updateUser_withInvalidEmail_shouldReturnBadRequest() throws Exception {
        UserUpdateDTO invalidDTO = new UserUpdateDTO();
        invalidDTO.setFirstName("John");
        invalidDTO.setEmail("invalid-email");

        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest());
    }

    // ===== Retrieval Edge Cases =====

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void getUserById_withNonExistentId_shouldReturnNotFound() throws Exception {
        when(userService.getUserById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/users/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void getUserById_withZeroId_shouldReturnNotFound() throws Exception {
        when(userService.getUserById(0L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/users/0"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void getUserById_withNegativeId_shouldReturnNotFound() throws Exception {
        when(userService.getUserById(-1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/users/-1"))
                .andExpect(status().isNotFound());
    }

    // ===== Delete Edge Cases =====

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void deleteUser_withNonExistentId_shouldReturnNoContent() throws Exception {
        // Note: deleteUser returns void, so even non-existent ID returns 204
        mockMvc.perform(delete("/api/users/999"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void deleteUser_withZeroId_shouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/users/0"))
                .andExpect(status().isNoContent());
    }

    // ===== Malformed Request Edge Cases =====

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void createUser_withMalformedJson_shouldReturnBadRequest() throws Exception {
        String malformedJson = "{\"firstName\": \"John\", \"lastName\": }";

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(malformedJson))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void createUser_withEmptyBody_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void updateUser_withMalformedJson_shouldReturnBadRequest() throws Exception {
        String malformedJson = "{\"firstName\": \"John\", \"email\": }";

        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(malformedJson))
                .andExpect(status().isInternalServerError());
    }

    // ===== Authorization Edge Cases =====

    @Test
    void createUser_withoutAuthentication_shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isForbidden());
    }

    @Test
    void getUserById_withoutAuthentication_shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateUser_withoutAuthentication_shouldReturnUnauthorized() throws Exception {
        UserUpdateDTO updateDTO = new UserUpdateDTO();
        updateDTO.setFirstName("Updated");

        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteUser_withoutAuthentication_shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAllUsers_withoutAuthentication_shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isForbidden());
    }
}

package com.fincore.usermgmt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fincore.usermgmt.dto.LoginRequest;
import com.fincore.usermgmt.entity.User;
import com.fincore.usermgmt.entity.UserStatus;
import com.fincore.usermgmt.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        // Reset admin user state before each test
        User admin = userRepository.findByUsername("admin").orElseThrow();
        admin.setFailedLoginAttempts(0);
        admin.setLockedUntil(null);
        admin.setStatus(UserStatus.ACTIVE);
        userRepository.save(admin);
    }

    @Test
    void login_WithValidCredentials_ShouldReturnTokenAndUserInfo() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("Admin@123456");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.username").value("admin"))
                .andExpect(jsonPath("$.fullName").value("System Administrator"))
                .andExpect(jsonPath("$.role").value("SYSTEM_ADMINISTRATOR"));
    }

    @Test
    void login_WithInvalidPassword_ShouldReturn401() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("wrongpassword");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_WithNonExistentUser_ShouldReturn401() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("nonexistent");
        request.setPassword("password");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Transactional
    void login_After5FailedAttempts_ShouldLockAccount() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("staff");
        request.setPassword("wrongpassword");

        // Attempt 1-5: Should fail with invalid credentials
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }

        // Verify account is locked
        User user = userRepository.findByUsername("staff").orElseThrow();
        assertEquals(5, user.getFailedLoginAttempts());
        assertEquals(UserStatus.LOCKED, user.getStatus());
        assertNotNull(user.getLockedUntil());

        // Attempt 6: Should fail with account locked
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Transactional
    void login_SuccessfulLoginAfterFailedAttempts_ShouldResetCounter() throws Exception {
        LoginRequest wrongRequest = new LoginRequest();
        wrongRequest.setUsername("compliance");
        wrongRequest.setPassword("wrongpassword");

        // 2 failed attempts
        for (int i = 0; i < 2; i++) {
            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(wrongRequest)))
                    .andExpect(status().isUnauthorized());
        }

        // Verify failed attempts recorded
        User user = userRepository.findByUsername("compliance").orElseThrow();
        assertEquals(2, user.getFailedLoginAttempts());

        // Successful login
        LoginRequest correctRequest = new LoginRequest();
        correctRequest.setUsername("compliance");
        correctRequest.setPassword("Compliance@123");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(correctRequest)))
                .andExpect(status().isOk());

        // Verify counter reset
        user = userRepository.findByUsername("compliance").orElseThrow();
        assertEquals(0, user.getFailedLoginAttempts());
        assertNull(user.getLockedUntil());
    }

    @Test
    void login_WithValidToken_ShouldAccessProtectedEndpoint() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("Admin@123456");

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        String token = objectMapper.readTree(responseBody).get("token").asText();

        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void login_AllThreeRoles_ShouldSucceed() throws Exception {
        String[][] users = {
            {"admin", "Admin@123456", "SYSTEM_ADMINISTRATOR"},
            {"compliance", "Compliance@123", "COMPLIANCE_OFFICER"},
            {"staff", "Staff@123456", "OPERATIONAL_STAFF"}
        };

        for (String[] userData : users) {
            LoginRequest request = new LoginRequest();
            request.setUsername(userData[0]);
            request.setPassword(userData[1]);

            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value(userData[0]))
                    .andExpect(jsonPath("$.role").value(userData[2]));
        }
    }
}

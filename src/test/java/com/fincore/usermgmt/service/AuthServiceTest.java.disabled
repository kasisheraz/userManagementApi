package com.fincore.usermgmt.service;

import com.fincore.usermgmt.dto.LoginRequest;
import com.fincore.usermgmt.dto.LoginResponse;
import com.fincore.usermgmt.entity.Role;
import com.fincore.usermgmt.entity.User;
import com.fincore.usermgmt.entity.UserStatus;
import com.fincore.usermgmt.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private Role testRole;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "maxLoginAttempts", 5);
        ReflectionTestUtils.setField(authService, "lockDuration", 1800);

        testRole = new Role();
        testRole.setId(1L);
        testRole.setName("SYSTEM_ADMINISTRATOR");

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setPassword("$2a$10$encodedPassword");
        testUser.setFullName("Test User");
        testUser.setEmail("test@test.com");
        testUser.setStatus(UserStatus.ACTIVE);
        testUser.setRole(testRole);
        testUser.setFailedLoginAttempts(0);
    }

    @Test
    void login_WithValidCredentials_ShouldReturnToken() {
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("password123");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", testUser.getPassword())).thenReturn(true);

        LoginResponse response = authService.login(request);

        assertNotNull(response);
        assertNotNull(response.getToken());
        assertEquals("testuser", response.getUsername());
        assertEquals("Test User", response.getFullName());
        assertEquals("SYSTEM_ADMINISTRATOR", response.getRole());
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    void login_WithInvalidUsername_ShouldThrowException() {
        LoginRequest request = new LoginRequest();
        request.setUsername("nonexistent");
        request.setPassword("password123");

        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> authService.login(request));
    }

    @Test
    void login_WithInvalidPassword_ShouldThrowException() {
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("wrongpassword");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongpassword", testUser.getPassword())).thenReturn(false);

        assertThrows(RuntimeException.class, () -> authService.login(request));
        verify(userRepository).save(testUser);
        assertEquals(1, testUser.getFailedLoginAttempts());
    }

    @Test
    void login_WithInactiveUser_ShouldThrowException() {
        testUser.setStatus(UserStatus.INACTIVE);
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("password123");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        assertThrows(RuntimeException.class, () -> authService.login(request));
    }

    @Test
    void login_WithLockedAccount_ShouldThrowException() {
        testUser.setLockedUntil(LocalDateTime.now().plusMinutes(30));
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("password123");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        assertThrows(RuntimeException.class, () -> authService.login(request));
    }

    @Test
    void login_After5FailedAttempts_ShouldLockAccount() {
        testUser.setFailedLoginAttempts(4);
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("wrongpassword");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongpassword", testUser.getPassword())).thenReturn(false);

        assertThrows(RuntimeException.class, () -> authService.login(request));
        
        assertEquals(5, testUser.getFailedLoginAttempts());
        assertEquals(UserStatus.LOCKED, testUser.getStatus());
        assertNotNull(testUser.getLockedUntil());
    }

    @Test
    void login_AfterSuccessfulLogin_ShouldResetFailedAttempts() {
        testUser.setFailedLoginAttempts(3);
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("password123");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", testUser.getPassword())).thenReturn(true);

        authService.login(request);

        assertEquals(0, testUser.getFailedLoginAttempts());
        assertNull(testUser.getLockedUntil());
        assertNotNull(testUser.getLastLoginAt());
    }
}

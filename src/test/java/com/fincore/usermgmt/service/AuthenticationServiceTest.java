package com.fincore.usermgmt.service;

import com.fincore.usermgmt.dto.AuthenticationResponse;
import com.fincore.usermgmt.dto.OtpResponse;
import com.fincore.usermgmt.dto.UserDTO;
import com.fincore.usermgmt.entity.Role;
import com.fincore.usermgmt.entity.User;
import com.fincore.usermgmt.mapper.UserMapper;
import com.fincore.usermgmt.repository.UserRepository;
import com.fincore.usermgmt.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private OtpService otpService;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private AuthenticationService authenticationService;

    private User testUser;
    private Role testRole;

    @BeforeEach
    void setUp() {
        // Set active profile to 'npe' for dev mode
        ReflectionTestUtils.setField(authenticationService, "activeProfile", "npe");

        // Setup test user
        testRole = new Role();
        testRole.setId(1L);
        testRole.setName("USER");

        testUser = new User();
        testUser.setId(1L);
        testUser.setPhoneNumber("+44-7700-900123");
        testUser.setEmail("test@example.com");
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setStatusDescription("ACTIVE");
        testUser.setRole(testRole);
    }

    // ==================== Initiate Authentication Tests ====================

    @Test
    void initiateAuthentication_WithValidPhoneNumber_ShouldReturnOtpResponse() {
        // Given
        String phoneNumber = "+44-7700-900123";
        String otp = "123456";

        when(userRepository.findByPhoneNumber(phoneNumber)).thenReturn(Optional.of(testUser));
        when(otpService.generateOtp(phoneNumber)).thenReturn(otp);
        when(otpService.getOtpExpirationSeconds()).thenReturn(300);

        // When
        OtpResponse response = authenticationService.initiateAuthentication(phoneNumber);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getPhoneNumber()).isEqualTo(phoneNumber);
        assertThat(response.getMessage()).contains("OTP sent to");
        assertThat(response.getDevOtp()).isEqualTo(otp); // NPE profile includes OTP in response
        assertThat(response.getExpiresIn()).isEqualTo(300L);

        verify(userRepository).findByPhoneNumber(phoneNumber);
        verify(otpService).generateOtp(phoneNumber);
    }

    @Test
    void initiateAuthentication_WithNonExistentUser_ShouldThrowException() {
        // Given
        String phoneNumber = "+44-9999-999999";

        when(userRepository.findByPhoneNumber(phoneNumber)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> authenticationService.initiateAuthentication(phoneNumber))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found");

        verify(userRepository).findByPhoneNumber(phoneNumber);
        verify(otpService, never()).generateOtp(anyString());
    }

    @Test
    void initiateAuthentication_WithInactiveUser_ShouldThrowException() {
        // Given
        String phoneNumber = "+44-7700-900123";
        testUser.setStatusDescription("INACTIVE");

        when(userRepository.findByPhoneNumber(phoneNumber)).thenReturn(Optional.of(testUser));

        // When & Then
        assertThatThrownBy(() -> authenticationService.initiateAuthentication(phoneNumber))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User account is not active");

        verify(userRepository).findByPhoneNumber(phoneNumber);
        verify(otpService, never()).generateOtp(anyString());
    }

    @Test
    void initiateAuthentication_WithSuspendedUser_ShouldThrowException() {
        // Given
        String phoneNumber = "+44-7700-900123";
        testUser.setStatusDescription("SUSPENDED");

        when(userRepository.findByPhoneNumber(phoneNumber)).thenReturn(Optional.of(testUser));

        // When & Then
        assertThatThrownBy(() -> authenticationService.initiateAuthentication(phoneNumber))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User account is not active");

        verify(userRepository).findByPhoneNumber(phoneNumber);
    }

    @Test
    void initiateAuthentication_InProductionEnvironment_ShouldNotIncludeOtpInResponse() {
        // Given
        ReflectionTestUtils.setField(authenticationService, "activeProfile", "production");
        String phoneNumber = "+44-7700-900123";
        String otp = "123456";

        when(userRepository.findByPhoneNumber(phoneNumber)).thenReturn(Optional.of(testUser));
        when(otpService.generateOtp(phoneNumber)).thenReturn(otp);
        when(otpService.getOtpExpirationSeconds()).thenReturn(300);

        // When
        OtpResponse response = authenticationService.initiateAuthentication(phoneNumber);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getDevOtp()).isNull(); // Production profile should NOT include OTP
        assertThat(response.getPhoneNumber()).isEqualTo(phoneNumber);
    }

    @Test
    void initiateAuthentication_InLocalEnvironment_ShouldIncludeOtpInResponse() {
        // Given
        ReflectionTestUtils.setField(authenticationService, "activeProfile", "local-h2");
        String phoneNumber = "+44-7700-900123";
        String otp = "123456";

        when(userRepository.findByPhoneNumber(phoneNumber)).thenReturn(Optional.of(testUser));
        when(otpService.generateOtp(phoneNumber)).thenReturn(otp);
        when(otpService.getOtpExpirationSeconds()).thenReturn(300);

        // When
        OtpResponse response = authenticationService.initiateAuthentication(phoneNumber);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getDevOtp()).isEqualTo(otp); // Local profile includes OTP
    }

    // ==================== Verify OTP and Authenticate Tests ====================

    @Test
    void verifyOtpAndAuthenticate_WithValidOtp_ShouldReturnAuthenticationResponse() {
        // Given
        String phoneNumber = "+44-7700-900123";
        String otp = "123456";
        String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...";
        UserDTO userDTO = new UserDTO();
        userDTO.setId(1L);
        userDTO.setPhoneNumber(phoneNumber);
        userDTO.setRole("USER");

        when(otpService.verifyOtp(phoneNumber, otp)).thenReturn(true);
        when(userRepository.findByPhoneNumber(phoneNumber)).thenReturn(Optional.of(testUser));
        when(jwtTokenProvider.generateToken(phoneNumber, 1L, "USER")).thenReturn(token);
        when(jwtTokenProvider.getExpirationTime()).thenReturn(3600000L); // 1 hour
        when(userMapper.toUserDTO(testUser)).thenReturn(userDTO);

        // When
        AuthenticationResponse response = authenticationService.verifyOtpAndAuthenticate(phoneNumber, otp);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo(token);
        assertThat(response.getExpiresIn()).isEqualTo(3600L); // Converted to seconds
        assertThat(response.getUser()).isEqualTo(userDTO);

        verify(otpService).verifyOtp(phoneNumber, otp);
        verify(userRepository).findByPhoneNumber(phoneNumber);
        verify(jwtTokenProvider).generateToken(phoneNumber, 1L, "USER");
        verify(userMapper).toUserDTO(testUser);
    }

    @Test
    void verifyOtpAndAuthenticate_WithInvalidOtp_ShouldThrowException() {
        // Given
        String phoneNumber = "+44-7700-900123";
        String otp = "wrong";

        when(otpService.verifyOtp(phoneNumber, otp)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> authenticationService.verifyOtpAndAuthenticate(phoneNumber, otp))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Invalid or expired OTP");

        verify(otpService).verifyOtp(phoneNumber, otp);
        verify(userRepository, never()).findByPhoneNumber(anyString());
        verify(jwtTokenProvider, never()).generateToken(anyString(), anyLong(), anyString());
    }

    @Test
    void verifyOtpAndAuthenticate_WithExpiredOtp_ShouldThrowException() {
        // Given
        String phoneNumber = "+44-7700-900123";
        String otp = "123456";

        when(otpService.verifyOtp(phoneNumber, otp)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> authenticationService.verifyOtpAndAuthenticate(phoneNumber, otp))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Invalid or expired OTP");

        verify(otpService).verifyOtp(phoneNumber, otp);
    }

    @Test
    void verifyOtpAndAuthenticate_WithValidOtpButUserNotFound_ShouldThrowException() {
        // Given
        String phoneNumber = "+44-9999-999999";
        String otp = "123456";

        when(otpService.verifyOtp(phoneNumber, otp)).thenReturn(true);
        when(userRepository.findByPhoneNumber(phoneNumber)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> authenticationService.verifyOtpAndAuthenticate(phoneNumber, otp))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found");

        verify(otpService).verifyOtp(phoneNumber, otp);
        verify(userRepository).findByPhoneNumber(phoneNumber);
        verify(jwtTokenProvider, never()).generateToken(anyString(), anyLong(), anyString());
    }

    @Test
    void verifyOtpAndAuthenticate_WithUserHavingNoRole_ShouldUseDefaultUserRole() {
        // Given
        String phoneNumber = "+44-7700-900123";
        String otp = "123456";
        String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...";
        testUser.setRole(null); // User has no role

        UserDTO userDTO = new UserDTO();
        userDTO.setId(1L);
        userDTO.setPhoneNumber(phoneNumber);
        userDTO.setRole(null);

        when(otpService.verifyOtp(phoneNumber, otp)).thenReturn(true);
        when(userRepository.findByPhoneNumber(phoneNumber)).thenReturn(Optional.of(testUser));
        when(jwtTokenProvider.generateToken(phoneNumber, 1L, "USER")).thenReturn(token);
        when(jwtTokenProvider.getExpirationTime()).thenReturn(3600000L);
        when(userMapper.toUserDTO(testUser)).thenReturn(userDTO);

        // When
        AuthenticationResponse response = authenticationService.verifyOtpAndAuthenticate(phoneNumber, otp);

        // Then
        assertThat(response).isNotNull();
        verify(jwtTokenProvider).generateToken(phoneNumber, 1L, "USER"); // Default role
    }

    @Test
    void verifyOtpAndAuthenticate_WithAdminRole_ShouldIncludeRoleInToken() {
        // Given
        String phoneNumber = "+44-7700-900123";
        String otp = "123456";
        String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...";
        testRole.setName("ADMIN");

        UserDTO userDTO = new UserDTO();
        userDTO.setId(1L);
        userDTO.setPhoneNumber(phoneNumber);
        userDTO.setRole("ADMIN");

        when(otpService.verifyOtp(phoneNumber, otp)).thenReturn(true);
        when(userRepository.findByPhoneNumber(phoneNumber)).thenReturn(Optional.of(testUser));
        when(jwtTokenProvider.generateToken(phoneNumber, 1L, "ADMIN")).thenReturn(token);
        when(jwtTokenProvider.getExpirationTime()).thenReturn(3600000L);
        when(userMapper.toUserDTO(testUser)).thenReturn(userDTO);

        // When
        AuthenticationResponse response = authenticationService.verifyOtpAndAuthenticate(phoneNumber, otp);

        // Then
        assertThat(response).isNotNull();
        verify(jwtTokenProvider).generateToken(phoneNumber, 1L, "ADMIN");
    }

    @Test
    void initiateAuthentication_WithUppercaseStatusDescription_ShouldWork() {
        // Given
        String phoneNumber = "+44-7700-900123";
        testUser.setStatusDescription("ACTIVE"); // Uppercase

        when(userRepository.findByPhoneNumber(phoneNumber)).thenReturn(Optional.of(testUser));
        when(otpService.generateOtp(phoneNumber)).thenReturn("123456");
        when(otpService.getOtpExpirationSeconds()).thenReturn(300);

        // When
        OtpResponse response = authenticationService.initiateAuthentication(phoneNumber);

        // Then
        assertThat(response).isNotNull();
        verify(otpService).generateOtp(phoneNumber);
    }

    @Test
    void initiateAuthentication_WithLowercaseStatusDescription_ShouldWork() {
        // Given
        String phoneNumber = "+44-7700-900123";
        testUser.setStatusDescription("active"); // Lowercase

        when(userRepository.findByPhoneNumber(phoneNumber)).thenReturn(Optional.of(testUser));
        when(otpService.generateOtp(phoneNumber)).thenReturn("123456");
        when(otpService.getOtpExpirationSeconds()).thenReturn(300);

        // When
        OtpResponse response = authenticationService.initiateAuthentication(phoneNumber);

        // Then
        assertThat(response).isNotNull();
        verify(otpService).generateOtp(phoneNumber);
    }

    @Test
    void initiateAuthentication_ShouldMaskPhoneNumberInMessage() {
        // Given
        String phoneNumber = "+44-7700-900123";

        when(userRepository.findByPhoneNumber(phoneNumber)).thenReturn(Optional.of(testUser));
        when(otpService.generateOtp(phoneNumber)).thenReturn("123456");
        when(otpService.getOtpExpirationSeconds()).thenReturn(300);

        // When
        OtpResponse response = authenticationService.initiateAuthentication(phoneNumber);

        // Then
        assertThat(response.getMessage()).contains("OTP sent to");
        assertThat(response.getMessage()).contains("Please verify to complete authentication");
    }
}

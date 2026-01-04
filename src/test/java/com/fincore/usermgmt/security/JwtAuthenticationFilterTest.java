package com.fincore.usermgmt.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter authenticationFilter;

    private static final String VALID_TOKEN = "valid.jwt.token";
    private static final String INVALID_TOKEN = "invalid.token";
    private static final String TEST_PHONE = "+1234567890";
    private static final Long TEST_USER_ID = 123L;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    // Successful Authentication Tests

    @Test
    void doFilterInternal_withValidToken_shouldSetAuthentication() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Bearer " + VALID_TOKEN);
        when(tokenProvider.validateToken(VALID_TOKEN)).thenReturn(true);
        when(tokenProvider.getPhoneNumberFromToken(VALID_TOKEN)).thenReturn(TEST_PHONE);
        when(tokenProvider.getUserIdFromToken(VALID_TOKEN)).thenReturn(TEST_USER_ID);

        authenticationFilter.doFilterInternal(request, response, filterChain);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNotNull();
        assertThat(auth.getPrincipal()).isEqualTo(TEST_PHONE);
        assertThat(auth.getCredentials()).isNull();
        assertThat(auth.getAuthorities()).hasSize(1);
        assertThat(auth.getAuthorities().iterator().next().getAuthority()).isEqualTo("ROLE_USER");
        
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_withValidToken_shouldCallFilterChain() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Bearer " + VALID_TOKEN);
        when(tokenProvider.validateToken(VALID_TOKEN)).thenReturn(true);
        when(tokenProvider.getPhoneNumberFromToken(VALID_TOKEN)).thenReturn(TEST_PHONE);
        when(tokenProvider.getUserIdFromToken(VALID_TOKEN)).thenReturn(TEST_USER_ID);

        authenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void doFilterInternal_withValidToken_shouldSetAuthenticationDetails() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Bearer " + VALID_TOKEN);
        when(tokenProvider.validateToken(VALID_TOKEN)).thenReturn(true);
        when(tokenProvider.getPhoneNumberFromToken(VALID_TOKEN)).thenReturn(TEST_PHONE);
        when(tokenProvider.getUserIdFromToken(VALID_TOKEN)).thenReturn(TEST_USER_ID);

        authenticationFilter.doFilterInternal(request, response, filterChain);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth.getDetails()).isNotNull();
    }

    // No Token Tests

    @Test
    void doFilterInternal_withNoAuthorizationHeader_shouldNotSetAuthentication() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn(null);

        authenticationFilter.doFilterInternal(request, response, filterChain);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNull();
        
        verify(tokenProvider, never()).validateToken(any());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_withEmptyAuthorizationHeader_shouldNotSetAuthentication() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("");

        authenticationFilter.doFilterInternal(request, response, filterChain);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNull();
        
        verify(tokenProvider, never()).validateToken(any());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_withWhitespaceAuthorizationHeader_shouldNotSetAuthentication() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("   ");

        authenticationFilter.doFilterInternal(request, response, filterChain);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNull();
        
        verify(tokenProvider, never()).validateToken(any());
        verify(filterChain).doFilter(request, response);
    }

    // Invalid Token Format Tests

    @Test
    void doFilterInternal_withTokenWithoutBearerPrefix_shouldNotSetAuthentication() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn(VALID_TOKEN);

        authenticationFilter.doFilterInternal(request, response, filterChain);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNull();
        
        verify(tokenProvider, never()).validateToken(any());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_withBearerOnlyNoToken_shouldNotSetAuthentication() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Bearer ");

        authenticationFilter.doFilterInternal(request, response, filterChain);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNull();
        
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_withBearerAndWhitespace_shouldNotSetAuthentication() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Bearer    ");

        authenticationFilter.doFilterInternal(request, response, filterChain);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNull();
        
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_withLowercaseBearer_shouldNotSetAuthentication() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("bearer " + VALID_TOKEN);

        authenticationFilter.doFilterInternal(request, response, filterChain);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNull();
        
        verify(tokenProvider, never()).validateToken(any());
        verify(filterChain).doFilter(request, response);
    }

    // Invalid Token Tests

    @Test
    void doFilterInternal_withInvalidToken_shouldNotSetAuthentication() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Bearer " + INVALID_TOKEN);
        when(tokenProvider.validateToken(INVALID_TOKEN)).thenReturn(false);

        authenticationFilter.doFilterInternal(request, response, filterChain);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNull();
        
        verify(tokenProvider).validateToken(INVALID_TOKEN);
        verify(tokenProvider, never()).getPhoneNumberFromToken(any());
        verify(tokenProvider, never()).getUserIdFromToken(any());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_withExpiredToken_shouldNotSetAuthentication() throws ServletException, IOException {
        String expiredToken = "expired.jwt.token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + expiredToken);
        when(tokenProvider.validateToken(expiredToken)).thenReturn(false);

        authenticationFilter.doFilterInternal(request, response, filterChain);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNull();
        
        verify(filterChain).doFilter(request, response);
    }

    // Exception Handling Tests

    @Test
    void doFilterInternal_whenTokenProviderThrowsException_shouldNotSetAuthentication() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Bearer " + VALID_TOKEN);
        when(tokenProvider.validateToken(VALID_TOKEN)).thenThrow(new RuntimeException("Token validation error"));

        authenticationFilter.doFilterInternal(request, response, filterChain);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNull();
        
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_whenGetPhoneNumberThrowsException_shouldNotSetAuthentication() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Bearer " + VALID_TOKEN);
        when(tokenProvider.validateToken(VALID_TOKEN)).thenReturn(true);
        when(tokenProvider.getPhoneNumberFromToken(VALID_TOKEN)).thenThrow(new RuntimeException("Phone extraction error"));

        authenticationFilter.doFilterInternal(request, response, filterChain);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNull();
        
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_whenGetUserIdThrowsException_shouldNotSetAuthentication() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Bearer " + VALID_TOKEN);
        when(tokenProvider.validateToken(VALID_TOKEN)).thenReturn(true);
        when(tokenProvider.getPhoneNumberFromToken(VALID_TOKEN)).thenReturn(TEST_PHONE);
        when(tokenProvider.getUserIdFromToken(VALID_TOKEN)).thenThrow(new RuntimeException("UserId extraction error"));

        authenticationFilter.doFilterInternal(request, response, filterChain);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNull();
        
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_whenFilterChainThrowsException_shouldPropagateException() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Bearer " + VALID_TOKEN);
        when(tokenProvider.validateToken(VALID_TOKEN)).thenReturn(true);
        when(tokenProvider.getPhoneNumberFromToken(VALID_TOKEN)).thenReturn(TEST_PHONE);
        when(tokenProvider.getUserIdFromToken(VALID_TOKEN)).thenReturn(TEST_USER_ID);
        doThrow(new ServletException("Filter chain error")).when(filterChain).doFilter(request, response);

        assertThatThrownBy(() -> authenticationFilter.doFilterInternal(request, response, filterChain))
                .isInstanceOf(ServletException.class)
                .hasMessage("Filter chain error");
    }

    // Token Extraction Edge Cases

    @Test
    void doFilterInternal_withMultipleSpacesAfterBearer_shouldExtractToken() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Bearer   " + VALID_TOKEN);
        when(tokenProvider.validateToken(anyString())).thenReturn(false);

        authenticationFilter.doFilterInternal(request, response, filterChain);

        // Should extract "  token" which is invalid
        verify(tokenProvider).validateToken(argThat(token -> token.startsWith("  ")));
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_withExtraTextAfterToken_shouldExtractEntireString() throws ServletException, IOException {
        String tokenWithExtra = VALID_TOKEN + " extra";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + tokenWithExtra);
        when(tokenProvider.validateToken(tokenWithExtra)).thenReturn(true);
        when(tokenProvider.getPhoneNumberFromToken(tokenWithExtra)).thenReturn(TEST_PHONE);
        when(tokenProvider.getUserIdFromToken(tokenWithExtra)).thenReturn(TEST_USER_ID);

        authenticationFilter.doFilterInternal(request, response, filterChain);

        verify(tokenProvider).validateToken(tokenWithExtra);
        verify(filterChain).doFilter(request, response);
    }

    // Phone Number Edge Cases

    @Test
    void doFilterInternal_withNullPhoneNumber_shouldStillSetAuthentication() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Bearer " + VALID_TOKEN);
        when(tokenProvider.validateToken(VALID_TOKEN)).thenReturn(true);
        when(tokenProvider.getPhoneNumberFromToken(VALID_TOKEN)).thenReturn(null);
        when(tokenProvider.getUserIdFromToken(VALID_TOKEN)).thenReturn(TEST_USER_ID);

        authenticationFilter.doFilterInternal(request, response, filterChain);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNotNull();
        assertThat(auth.getPrincipal()).isNull();
        
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_withEmptyPhoneNumber_shouldSetAuthentication() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Bearer " + VALID_TOKEN);
        when(tokenProvider.validateToken(VALID_TOKEN)).thenReturn(true);
        when(tokenProvider.getPhoneNumberFromToken(VALID_TOKEN)).thenReturn("");
        when(tokenProvider.getUserIdFromToken(VALID_TOKEN)).thenReturn(TEST_USER_ID);

        authenticationFilter.doFilterInternal(request, response, filterChain);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNotNull();
        assertThat(auth.getPrincipal()).isEqualTo("");
        
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_withSpecialCharactersInPhoneNumber_shouldSetAuthentication() throws ServletException, IOException {
        String specialPhone = "+1-234-567-8900 ext.123";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + VALID_TOKEN);
        when(tokenProvider.validateToken(VALID_TOKEN)).thenReturn(true);
        when(tokenProvider.getPhoneNumberFromToken(VALID_TOKEN)).thenReturn(specialPhone);
        when(tokenProvider.getUserIdFromToken(VALID_TOKEN)).thenReturn(TEST_USER_ID);

        authenticationFilter.doFilterInternal(request, response, filterChain);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNotNull();
        assertThat(auth.getPrincipal()).isEqualTo(specialPhone);
        
        verify(filterChain).doFilter(request, response);
    }

    // Security Context Tests

    @Test
    void doFilterInternal_shouldNotAffectExistingAuthenticationIfTokenInvalid() throws ServletException, IOException {
        // Set up existing authentication
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(mock(Authentication.class));
        SecurityContextHolder.setContext(context);
        Authentication existingAuth = SecurityContextHolder.getContext().getAuthentication();

        when(request.getHeader("Authorization")).thenReturn("Bearer " + INVALID_TOKEN);
        when(tokenProvider.validateToken(INVALID_TOKEN)).thenReturn(false);

        authenticationFilter.doFilterInternal(request, response, filterChain);

        // Existing authentication should remain unchanged
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isSameAs(existingAuth);
        
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_withValidToken_shouldReplaceExistingAuthentication() throws ServletException, IOException {
        // Set up existing authentication
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(mock(Authentication.class));
        SecurityContextHolder.setContext(context);

        when(request.getHeader("Authorization")).thenReturn("Bearer " + VALID_TOKEN);
        when(tokenProvider.validateToken(VALID_TOKEN)).thenReturn(true);
        when(tokenProvider.getPhoneNumberFromToken(VALID_TOKEN)).thenReturn(TEST_PHONE);
        when(tokenProvider.getUserIdFromToken(VALID_TOKEN)).thenReturn(TEST_USER_ID);

        authenticationFilter.doFilterInternal(request, response, filterChain);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNotNull();
        assertThat(auth.getPrincipal()).isEqualTo(TEST_PHONE);
        
        verify(filterChain).doFilter(request, response);
    }

    // Authority Tests

    @Test
    void doFilterInternal_shouldAlwaysSetRoleUser() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Bearer " + VALID_TOKEN);
        when(tokenProvider.validateToken(VALID_TOKEN)).thenReturn(true);
        when(tokenProvider.getPhoneNumberFromToken(VALID_TOKEN)).thenReturn(TEST_PHONE);
        when(tokenProvider.getUserIdFromToken(VALID_TOKEN)).thenReturn(TEST_USER_ID);

        authenticationFilter.doFilterInternal(request, response, filterChain);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth.getAuthorities())
                .hasSize(1)
                .extracting("authority")
                .containsExactly("ROLE_USER");
    }

    // Integration Tests

    @Test
    void doFilterInternal_multipleRequests_shouldHandleIndependently() throws ServletException, IOException {
        // First request with valid token
        when(request.getHeader("Authorization")).thenReturn("Bearer " + VALID_TOKEN);
        when(tokenProvider.validateToken(VALID_TOKEN)).thenReturn(true);
        when(tokenProvider.getPhoneNumberFromToken(VALID_TOKEN)).thenReturn(TEST_PHONE);
        when(tokenProvider.getUserIdFromToken(VALID_TOKEN)).thenReturn(TEST_USER_ID);

        authenticationFilter.doFilterInternal(request, response, filterChain);
        Authentication firstAuth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(firstAuth).isNotNull();

        // Clear context for second request
        SecurityContextHolder.clearContext();

        // Second request without token
        when(request.getHeader("Authorization")).thenReturn(null);

        authenticationFilter.doFilterInternal(request, response, filterChain);
        Authentication secondAuth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(secondAuth).isNull();

        verify(filterChain, times(2)).doFilter(request, response);
    }
}

package com.fincore.usermgmt.security;

import com.fincore.usermgmt.entity.User;
import com.fincore.usermgmt.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.User.UserBuilder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String requestPath = request.getRequestURI();
            String jwt = getJwtFromRequest(request);

            if (!StringUtils.hasText(jwt)) {
                log.debug("No JWT token found in request to: {}", requestPath);
            } else {
                log.debug("JWT token found for request to: {}", requestPath);
                
                if (tokenProvider.validateToken(jwt)) {
                    String phoneNumber = tokenProvider.getPhoneNumberFromToken(jwt);
                    Long userId = tokenProvider.getUserIdFromToken(jwt);

                    // Load user from database to ensure they still exist and are active
                    User user = userRepository.findByPhoneNumber(phoneNumber).orElse(null);
                    
                    if (user != null && "ACTIVE".equalsIgnoreCase(user.getStatusDescription())) {
                        // Create a proper UserDetails object
                        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
                        if (user.getRole() != null && user.getRole().getName() != null) {
                            authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().getName().toUpperCase()));
                        } else {
                            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
                        }
                        
                        UserDetails userDetails = org.springframework.security.core.userdetails.User
                                .withUsername(phoneNumber)
                                .password("") // Not used for JWT auth
                                .authorities(authorities)
                                .build();

                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                userDetails, null, authorities);
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        log.info("Successfully authenticated user: {} (ID: {}) for request: {}", phoneNumber, userId, requestPath);
                    } else {
                        log.warn("User not found or inactive for phone: {} - denying access to: {}", phoneNumber, requestPath);
                    }
                } else {
                    log.warn("JWT token validation failed for request to: {}", requestPath);
                }
            }
        } catch (Exception ex) {
            log.error("Could not set user authentication in security context for request: {}", request.getRequestURI(), ex);
        }

        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}

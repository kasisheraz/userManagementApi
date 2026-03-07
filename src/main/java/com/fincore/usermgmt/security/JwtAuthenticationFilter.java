package com.fincore.usermgmt.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    
    @Autowired
    private JwtTokenProvider tokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        log.info("JWT Filter running for: {}", request.getRequestURI());
        
        try {
            String jwt = getJwtFromRequest(request);
            log.info("JWT token present: {}", jwt != null);

            if (StringUtils.hasText(jwt)) {
                log.info("JWT token found, validating...");
                boolean isValid = tokenProvider.validateToken(jwt);
                log.info("JWT token valid: {}", isValid);
                
                if (isValid) {
                    String phoneNumber = tokenProvider.getPhoneNumberFromToken(jwt);
                    log.info("Phone number from token: {}", phoneNumber);
                    
                    // Create simple authentication without database lookup
                    List<SimpleGrantedAuthority> authorities = new ArrayList<>();
                    authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
                    
                    UserDetails userDetails = org.springframework.security.core.userdetails.User
                            .withUsername(phoneNumber)
                            .password("")
                            .authorities(authorities)
                            .build();

                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userDetails, null, authorities);
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    log.info("✅ Authentication set for: {}", phoneNumber);
                } else {
                    log.warn("❌ JWT token validation failed");
                }
            } else {
                log.info("No JWT token in request");
            }
        } catch (Exception ex) {
            log.error("❌ Auth error: {}", ex.getMessage(), ex);
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

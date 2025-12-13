package com.fincore.usermgmt.security;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
@RequiredArgsConstructor
public class SimpleApiKeyInterceptor implements HandlerInterceptor {

    private static final String API_KEY_HEADER = "X-API-Key";
    private static final String VALID_API_KEY = "fincore-api-key-2024"; // Simple API key

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // Skip authentication for public endpoints
        String requestURI = request.getRequestURI();
        if (requestURI.startsWith("/actuator") || 
            requestURI.startsWith("/h2-console") || 
            requestURI.startsWith("/api/auth/")) {
            return true;
        }

        String apiKey = request.getHeader(API_KEY_HEADER);
        
        if (apiKey == null || !apiKey.equals(VALID_API_KEY)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\": \"Unauthorized - Invalid or missing API Key\"}");
            return false;
        }

        return true;
    }
}

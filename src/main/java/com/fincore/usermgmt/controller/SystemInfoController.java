package com.fincore.usermgmt.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/system")
public class SystemInfoController {

    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getSystemInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("timestamp", LocalDateTime.now().toString());
        info.put("version", "1.0.0");
        info.put("build", System.getenv().getOrDefault("BUILD_NUMBER", "unknown"));
        info.put("javaVersion", System.getProperty("java.version"));
        info.put("status", "running");
        info.put("message", "Build deployed successfully");
        return ResponseEntity.ok(info);
    }
    
    @GetMapping("/auth-test")
    public ResponseEntity<Map<String, Object>> testAuth(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        Map<String, Object> info = new HashMap<>();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        info.put("hasAuthHeader", authHeader != null);
        info.put("authHeaderLength", authHeader != null ? authHeader.length() : 0);
        info.put("authHeaderPreview", authHeader != null ? authHeader.substring(0, Math.min(30, authHeader.length())) + "..." : "null");
        info.put("isAuthenticated", auth != null && auth.isAuthenticated());
        info.put("authType", auth != null ? auth.getClass().getSimpleName() : "null");
        info.put("principal", auth != null ? auth.getPrincipal().toString() : "null");
        info.put("authorities", auth != null ? auth.getAuthorities().toString() : "null");
        info.put("message", "Check if JWT authentication is working");
        info.put("WARNING", "This endpoint is permitAll(), so it allows anonymous access!");
        
        return ResponseEntity.ok(info);
    }
    
    @GetMapping("/protected-test")
    public ResponseEntity<Map<String, Object>> protectedTest() {
        Map<String, Object> info = new HashMap<>();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        info.put("message", "If you see this, JWT auth is working!");
        info.put("principal", auth != null ? auth.getPrincipal().toString() : "null");
        info.put("authorities", auth != null ? auth.getAuthorities().toString() : "null");
        
        return ResponseEntity.ok(info);
    }
}

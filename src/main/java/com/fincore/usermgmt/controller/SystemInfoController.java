package com.fincore.usermgmt.controller;

import com.fincore.usermgmt.dto.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "System Information", description = "APIs for system information, health checks, and authentication testing")
public class SystemInfoController {

    @GetMapping("/info")
    @Operation(
        summary = "Get system information",
        description = "Retrieves system information including version, build number, Java version, and current status. This endpoint is publicly accessible."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved system information",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class)))
    })
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
    @Operation(
        summary = "Test JWT authentication (debug endpoint)",
        description = "Debug endpoint to test if JWT authentication is working properly. Returns authentication details including header presence, authentication status, and user authorities. WARNING: This endpoint is permitAll() and allows anonymous access."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved authentication information",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class)))
    })
    public ResponseEntity<Map<String, Object>> testAuth(
            @Parameter(description = "Authorization header with Bearer token", example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
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
    @Operation(
        summary = "Test protected endpoint",
        description = "Protected endpoint that requires valid JWT authentication. If you can access this endpoint, JWT authentication is working correctly. Returns principal and authority information."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully authenticated and retrieved information",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Map<String, Object>> protectedTest() {
        Map<String, Object> info = new HashMap<>();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        info.put("message", "If you see this, JWT auth is working!");
        info.put("principal", auth != null ? auth.getPrincipal().toString() : "null");
        info.put("authorities", auth != null ? auth.getAuthorities().toString() : "null");
        
        return ResponseEntity.ok(info);
    }
}

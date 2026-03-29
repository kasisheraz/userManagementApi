package com.fincore.usermgmt.controller;

import com.fincore.usermgmt.dto.*;
import com.fincore.usermgmt.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "APIs for user authentication with OTP")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @PostMapping("/request-otp")
    @Operation(
        summary = "Request OTP",
        description = "Sends a one-time password (OTP) to the provided phone number for authentication"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OTP sent successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = OtpResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid phone number format",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<OtpResponse> requestOtp(
        @Parameter(description = "Authentication request with phone number", required = true)
        @Valid @RequestBody AuthenticationRequest request
    ) {
        OtpResponse response = authenticationService.initiateAuthentication(request.getPhoneNumber());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify-otp")
    @Operation(
        summary = "Verify OTP and get JWT token",
        description = "Verifies the OTP code and returns a JWT access token for authenticated API calls"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OTP verified successfully, JWT token returned",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthenticationResponse.class))),
        @ApiResponse(responseCode = "401", description = "Invalid or expired OTP",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "User not found",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<AuthenticationResponse> verifyOtp(
        @Parameter(description = "OTP verification request with phone number and OTP code", required = true)
        @Valid @RequestBody OtpVerificationRequest request
    ) {
        AuthenticationResponse response = authenticationService.verifyOtpAndAuthenticate(
                request.getPhoneNumber(), 
                request.getOtp()
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    @Operation(
        summary = "Get current user",
        description = "Returns information about the currently authenticated user"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved authenticated user"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<String> getCurrentUser(
        @Parameter(description = "JWT Bearer token", required = true, example = "Bearer eyJhbGc...")
        @RequestHeader("Authorization") String authorization
    ) {
        return ResponseEntity.ok("Authenticated user");
    }
}

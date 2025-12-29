package com.fincore.usermgmt.controller;

import com.fincore.usermgmt.dto.*;
import com.fincore.usermgmt.service.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @PostMapping("/request-otp")
    public ResponseEntity<OtpResponse> requestOtp(@Valid @RequestBody AuthenticationRequest request) {
        OtpResponse response = authenticationService.initiateAuthentication(request.getPhoneNumber());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<AuthenticationResponse> verifyOtp(@Valid @RequestBody OtpVerificationRequest request) {
        AuthenticationResponse response = authenticationService.verifyOtpAndAuthenticate(
                request.getPhoneNumber(), 
                request.getOtp()
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<String> getCurrentUser(@RequestHeader("Authorization") String authorization) {
        return ResponseEntity.ok("Authenticated user");
    }
}

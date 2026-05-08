package com.fincore.usermgmt.controller;

import com.fincore.usermgmt.dto.sumsub.SumSubVerificationStatus;
import com.fincore.usermgmt.service.sumsub.MockSumSubService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Mock SumSub controller for testing verification workflows
 * Only available in NPE/local environments
 * Allows simulating verification completion without real SumSub
 */
@RestController
@RequestMapping("/api/mock/sumsub")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Mock SumSub", description = "Mock SumSub endpoints for testing (NPE only)")
@Profile({"npe", "local", "test"})
public class MockSumSubController {

    private final MockSumSubService mockSumSubService;

    @PostMapping("/applicants/{applicantId}/simulate-approval")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Simulate verification approval (mock only)")
    public ResponseEntity<SumSubVerificationStatus> simulateApproval(
            @PathVariable String applicantId) {
        
        log.info("🎭 MOCK: Simulating approval for applicant {}", applicantId);
        
        mockSumSubService.simulateVerificationComplete(applicantId, true);
        SumSubVerificationStatus status = mockSumSubService.getVerificationStatus(applicantId);
        
        return ResponseEntity.ok(status);
    }

    @PostMapping("/applicants/{applicantId}/simulate-rejection")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Simulate verification rejection (mock only)")
    public ResponseEntity<SumSubVerificationStatus> simulateRejection(
            @PathVariable String applicantId) {
        
        log.info("🎭 MOCK: Simulating rejection for applicant {}", applicantId);
        
        mockSumSubService.simulateVerificationComplete(applicantId, false);
        SumSubVerificationStatus status = mockSumSubService.getVerificationStatus(applicantId);
        
        return ResponseEntity.ok(status);
    }

    @GetMapping("/applicants/{applicantId}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'USER')")
    @Operation(summary = "Get mock verification status")
    public ResponseEntity<SumSubVerificationStatus> getStatus(
            @PathVariable String applicantId) {
        
        log.info("🎭 MOCK: Getting status for applicant {}", applicantId);
        
        SumSubVerificationStatus status = mockSumSubService.getVerificationStatus(applicantId);
        return ResponseEntity.ok(status);
    }
}

package com.fincore.usermgmt.controller;

import com.fincore.usermgmt.dto.*;
import com.fincore.usermgmt.service.OrganisationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Organisation management endpoints.
 */
@RestController
@RequestMapping("/api/organisations")
@RequiredArgsConstructor
@Slf4j
public class OrganisationController {

    private final OrganisationService organisationService;

    /**
     * Create a new organisation.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<OrganisationDTO> createOrganisation(
            @Valid @RequestBody OrganisationCreateDTO createDTO) {
        log.info("REST request to create organisation: {}", createDTO.getLegalName());
        OrganisationDTO created = organisationService.createOrganisation(createDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Get organisation by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<OrganisationDTO> getOrganisationById(@PathVariable Long id) {
        log.info("REST request to get organisation by ID: {}", id);
        return organisationService.getOrganisationById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get all organisations with pagination.
     */
    @GetMapping
    public ResponseEntity<PagedResponse<OrganisationDTO>> getAllOrganisations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "legalName") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection) {
        log.info("REST request to get all organisations - page: {}, size: {}", page, size);
        PagedResponse<OrganisationDTO> response = organisationService.getAllOrganisations(page, size, sortBy, sortDirection);
        return ResponseEntity.ok(response);
    }

    /**
     * Search organisations with filters.
     */
    @PostMapping("/search")
    public ResponseEntity<PagedResponse<OrganisationDTO>> searchOrganisations(
            @RequestBody OrganisationSearchDTO searchDTO) {
        log.info("REST request to search organisations");
        PagedResponse<OrganisationDTO> response = organisationService.searchOrganisations(searchDTO);
        return ResponseEntity.ok(response);
    }

    /**
     * Get organisations by owner.
     */
    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<List<OrganisationDTO>> getOrganisationsByOwner(@PathVariable Long ownerId) {
        log.info("REST request to get organisations for owner: {}", ownerId);
        List<OrganisationDTO> organisations = organisationService.getOrganisationsByOwner(ownerId);
        return ResponseEntity.ok(organisations);
    }

    /**
     * Get organisations by status.
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<OrganisationDTO>> getOrganisationsByStatus(@PathVariable String status) {
        log.info("REST request to get organisations by status: {}", status);
        try {
            List<OrganisationDTO> organisations = organisationService.getOrganisationsByStatus(status);
            return ResponseEntity.ok(organisations);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Update an organisation.
     */
    @PutMapping("/{id}")
    public ResponseEntity<OrganisationDTO> updateOrganisation(
            @PathVariable Long id,
            @Valid @RequestBody OrganisationUpdateDTO updateDTO) {
        log.info("REST request to update organisation ID: {}", id);
        try {
            OrganisationDTO updated = organisationService.updateOrganisation(id, updateDTO);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            throw e;
        }
    }

    /**
     * Update organisation status.
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<OrganisationDTO> updateOrganisationStatus(
            @PathVariable Long id,
            @RequestParam String status,
            @RequestParam(required = false) String reason) {
        log.info("REST request to update organisation status - ID: {}, Status: {}", id, status);
        try {
            OrganisationDTO updated = organisationService.updateOrganisationStatus(id, status, reason);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            throw e;
        }
    }

    /**
     * Delete an organisation.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrganisation(@PathVariable Long id) {
        log.info("REST request to delete organisation ID: {}", id);
        try {
            organisationService.deleteOrganisation(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            throw e;
        }
    }

    /**
     * Check if registration number exists.
     */
    @GetMapping("/exists/registration/{registrationNumber}")
    public ResponseEntity<Boolean> checkRegistrationNumberExists(@PathVariable String registrationNumber) {
        log.info("REST request to check registration number: {}", registrationNumber);
        boolean exists = organisationService.existsByRegistrationNumber(registrationNumber);
        return ResponseEntity.ok(exists);
    }
}

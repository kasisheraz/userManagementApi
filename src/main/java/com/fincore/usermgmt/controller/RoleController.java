package com.fincore.usermgmt.controller;

import com.fincore.usermgmt.dto.RoleDTO;
import com.fincore.usermgmt.entity.Role;
import com.fincore.usermgmt.repository.RoleRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST Controller for Role management endpoints.
 * Provides endpoints to fetch available roles in the system.
 */
@Slf4j
@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
@Tag(name = "Role Management", description = "APIs for fetching available roles in the system")
@SecurityRequirement(name = "bearerAuth")
public class RoleController {

    private final RoleRepository roleRepository;

    /**
     * Get all roles available in the system.
     * GET /api/roles
     * 
     * @return List of roles with id, name, and description
     */
    @GetMapping
    @Operation(
        summary = "Get all roles",
        description = "Retrieves all roles available in the system for user assignment"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved list of roles",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = RoleDTO.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token")
    })
    public List<RoleDTO> getAllRoles() {
        log.info("Fetching all available roles");
        
        List<Role> roles = roleRepository.findAll();
        
        return roles.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Convert Role entity to RoleDTO
     */
    private RoleDTO toDTO(Role role) {
        RoleDTO dto = new RoleDTO();
        dto.setId(role.getId());
        dto.setName(role.getName());
        dto.setDescription(role.getDescription());
        return dto;
    }
}

package com.fincore.usermgmt.controller;

import com.fincore.usermgmt.dto.ErrorResponse;
import com.fincore.usermgmt.dto.UserCreateDTO;
import com.fincore.usermgmt.dto.UserDTO;
import com.fincore.usermgmt.dto.UserUpdateDTO;
import com.fincore.usermgmt.service.UserService;
import com.fincore.usermgmt.util.RoleSecurity;
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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "APIs for managing users in the system")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    @GetMapping
    @Operation(
        summary = "Get all users",
        description = "Retrieves a list of all users in the system. Protected role users (ADMIN, SUPER_ADMIN, SYSTEM_ADMINISTRATOR) are filtered out."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved list of users",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDTO.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public List<UserDTO> getAllUsers() {
        // Filter out protected role users (ADMIN, SUPER_ADMIN, SYSTEM_ADMINISTRATOR)
        return userService.getAllUsers().stream()
                .filter(user -> !RoleSecurity.isProtectedRole(user.getRole()))
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Get user by ID",
        description = "Retrieves a specific user by their unique identifier"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved user",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDTO.class))),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<UserDTO> getUserById(
        @Parameter(description = "User ID", required = true, example = "1")
        @PathVariable Long id
    ) {
        return userService.getUserById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
        summary = "Create a new user",
        description = "Creates a new user with the provided information. Protected roles cannot be assigned during creation."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "User created successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "409", description = "Email or phone number already exists",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Object> createUser(
        @Parameter(description = "User creation data", required = true)
        @Valid @RequestBody UserCreateDTO userCreateDTO
    ) {
        // Validate and sanitize role only when provided - prevent protected role creation
        if (userCreateDTO.getRole() != null) {
            String sanitizedRole = RoleSecurity.validateRoleForCreation(userCreateDTO.getRole());
            userCreateDTO.setRole(sanitizedRole);
        }
        try {
            UserDTO createdUser = userService.createUser(userCreateDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
        } catch (DataIntegrityViolationException e) {
            String errorMessage = "Email or phone number already exists";
            if (e.getMessage() != null && e.getMessage().contains("EMAIL")) {
                errorMessage = "Email already exists";
            } else if (e.getMessage() != null && e.getMessage().contains("PHONE")) {
                errorMessage = "Phone number already exists";
            }
            return ResponseEntity.status(HttpStatus.CONFLICT).body(
                    new ErrorResponse(errorMessage, HttpStatus.CONFLICT.value())
            );
        }
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "Update user",
        description = "Updates an existing user's information. Users with protected roles (ADMIN, SUPER_ADMIN, SYSTEM_ADMINISTRATOR) cannot be modified."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User updated successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Forbidden - Cannot modify users with protected roles",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<?> updateUser(
        @Parameter(description = "User ID", required = true, example = "1")
        @PathVariable Long id,
        @Parameter(description = "User update data", required = true)
        @Valid @RequestBody UserUpdateDTO userUpdateDTO
    ) {
        // Check if user exists and has protected role
        return userService.getUserById(id)
                .map(existingUser -> {
                    if (RoleSecurity.isProtectedRole(existingUser.getRole())) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                .body(new ErrorResponse(
                                        "Cannot modify users with protected roles (ADMIN, SUPER_ADMIN, SYSTEM_ADMINISTRATOR)",
                                        HttpStatus.FORBIDDEN.value()
                                ));
                    }
                    try {
                        UserDTO updatedUser = userService.updateUser(id, userUpdateDTO);
                        return ResponseEntity.ok((Object) updatedUser);
                    } catch (RuntimeException e) {
                        throw e;
                    }
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Delete user",
        description = "Deletes a user from the system. Users with protected roles (ADMIN, SUPER_ADMIN, SYSTEM_ADMINISTRATOR) cannot be deleted."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "User deleted successfully"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Cannot delete users with protected roles",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<?> deleteUser(
        @Parameter(description = "User ID", required = true, example = "1")
        @PathVariable Long id
    ) {
        // Check if user exists and has protected role
        return userService.getUserById(id)
                .map(existingUser -> {
                    if (RoleSecurity.isProtectedRole(existingUser.getRole())) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                .body(new ErrorResponse(
                                        "Cannot delete users with protected roles (ADMIN, SUPER_ADMIN, SYSTEM_ADMINISTRATOR)",
                                        HttpStatus.FORBIDDEN.value()
                                ));
                    }
                    userService.deleteUser(id);
                    return ResponseEntity.noContent().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}

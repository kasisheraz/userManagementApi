package com.fincore.usermgmt.controller;

import com.fincore.usermgmt.dto.ErrorResponse;
import com.fincore.usermgmt.dto.UserCreateDTO;
import com.fincore.usermgmt.dto.UserDTO;
import com.fincore.usermgmt.dto.UserUpdateDTO;
import com.fincore.usermgmt.service.UserService;
import com.fincore.usermgmt.util.RoleSecurity;
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
public class UserController {

    private final UserService userService;

    @GetMapping
    public List<UserDTO> getAllUsers() {
        // Filter out protected role users (ADMIN, SUPER_ADMIN, SYSTEM_ADMINISTRATOR)
        return userService.getAllUsers().stream()
                .filter(user -> !RoleSecurity.isProtectedRole(user.getRole()))
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        return userService.getUserById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Object> createUser(@Valid @RequestBody UserCreateDTO userCreateDTO) {
        // Validate and sanitize role - prevent protected role creation
        String sanitizedRole = RoleSecurity.validateRoleForCreation(userCreateDTO.getRole());
        userCreateDTO.setRole(sanitizedRole);
        
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
    public ResponseEntity<?> updateUser(@PathVariable Long id, @Valid @RequestBody UserUpdateDTO userUpdateDTO) {
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
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
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

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

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public List<UserDTO> getAllUsers() {
        // Filter out admin users from the response
        return userService.getAllUsers().stream()
                .filter(user -> !RoleSecurity.isProtectedRole(user.getRole()))
                .collect(java.util.stream.Collectors.toList());
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
        try {
            // Security check: Validate role for creation
            if (userCreateDTO.getRole() != null) {
                try {
                    RoleSecurity.validateRoleForCreation(userCreateDTO.getRole());
                } catch (IllegalArgumentException | SecurityException e) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                            new ErrorResponse(e.getMessage(), HttpStatus.FORBIDDEN.value())
                    );
                }
            }
            
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
    public ResponseEntity<Object> updateUser(@PathVariable Long id, @Valid @RequestBody UserUpdateDTO userUpdateDTO) {
        try {
            // Security check: Prevent modification of admin users
            UserDTO existingUser = userService.getUserById(id)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            if (RoleSecurity.isProtectedRole(existingUser.getRole())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                        new ErrorResponse(
                                "Cannot modify users with admin roles. User role: " + existingUser.getRole(),
                                HttpStatus.FORBIDDEN.value()
                        )
                );
            }
            
            // Additional check: Validate new role if being changed
            if (userUpdateDTO.getRole() != null) {
                try {
                    RoleSecurity.validateRoleForCreation(userUpdateDTO.getRole());
                } catch (IllegalArgumentException | SecurityException e) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                            new ErrorResponse(e.getMessage(), HttpStatus.FORBIDDEN.value())
                    );
                }
            }
            
            return ResponseEntity.ok(userService.updateUser(id, userUpdateDTO));
        } catch (RuntimeException e) {
            if ("User not found".equals(e.getMessage())) {
                return ResponseEntity.notFound().build();
            }
            throw e;
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteUser(@PathVariable Long id) {
        // Security check: Prevent deletion of admin users
        UserDTO user = userService.getUserById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        
        if (RoleSecurity.isProtectedRole(user.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    new ErrorResponse(
                            "Cannot delete users with admin roles. User role: " + user.getRole(),
                            HttpStatus.FORBIDDEN.value()
                    )
            );
        }
        
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}

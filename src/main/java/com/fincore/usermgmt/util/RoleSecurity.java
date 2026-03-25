package com.fincore.usermgmt.util;

import java.util.Arrays;
import java.util.List;

/**
 * Role Security Utility
 * 
 * Provides centralized role validation and protection logic.
 * Prevents unauthorized access to admin-level accounts.
 */
public class RoleSecurity {
    
    /**
     * List of protected roles that cannot be created, modified, or deleted
     * through standard user management endpoints
     */
    public static final List<String> PROTECTED_ROLES = Arrays.asList(
        "ADMIN",
        "SUPER_ADMIN",
        "SYSTEM_ADMINISTRATOR"
    );
    
    /**
     * Roles that can be created through the standard user creation endpoint
     */
    public static final List<String> CREATABLE_ROLES = Arrays.asList(
        "USER",
        "MANAGER"
    );
    
    /**
     * Check if a role is protected (admin-level)
     * 
     * @param role The role to check
     * @return true if the role is protected, false otherwise
     */
    public static boolean isProtectedRole(String role) {
        if (role == null || role.isEmpty()) {
            return false;
        }
        return PROTECTED_ROLES.contains(role.toUpperCase());
    }
    
    /**
     * Check if a role can be created through standard endpoints
     * 
     * @param role The role to check
     * @return true if the role can be created, false otherwise
     */
    public static boolean isCreatableRole(String role) {
        if (role == null || role.isEmpty()) {
            return false;
        }
        return CREATABLE_ROLES.contains(role.toUpperCase());
    }
    
    /**
     * Validate that a role can be assigned during user creation
     * 
     * @param requestedRole The role being requested
     * @throws SecurityException if the role is protected
     * @throws IllegalArgumentException if the role is invalid
     */
    public static void validateRoleForCreation(String requestedRole) {
        if (requestedRole == null || requestedRole.isEmpty()) {
            return; // Will default to USER
        }
        
        String role = requestedRole.toUpperCase();
        
        if (isProtectedRole(role)) {
            throw new SecurityException(
                "Cannot create users with admin roles (" + role + ") via this endpoint. " +
                "Use /api/admin-management/create-admin endpoint instead."
            );
        }
        
        if (!isCreatableRole(role)) {
            throw new IllegalArgumentException(
                "Invalid role '" + role + "'. Allowed values: USER, MANAGER"
            );
        }
    }
    
    /**
     * Get the default role for new users
     * 
     * @return The default role string
     */
    public static String getDefaultRole() {
        return "USER";
    }
}

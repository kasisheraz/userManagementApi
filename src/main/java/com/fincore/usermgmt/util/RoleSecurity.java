package com.fincore.usermgmt.util;

import java.util.Arrays;
import java.util.List;

/**
 * Utility class for role-based security operations
 * Defines protected roles and provides validation methods
 */
public class RoleSecurity {
    
    // Protected roles that cannot be created/modified/deleted via API
    private static final List<String> PROTECTED_ROLES = Arrays.asList(
        "ADMIN",
        "SUPER_ADMIN", 
        "SYSTEM_ADMINISTRATOR"
    );
    
    // Roles that can be created via API
    private static final List<String> CREATABLE_ROLES = Arrays.asList(
        "USER",
        "MANAGER"
    );
    
    // Default role for new users
    private static final String DEFAULT_ROLE = "USER";
    
    /**
     * Check if a role is protected from modification
     */
    public static boolean isProtectedRole(String role) {
        return role != null && PROTECTED_ROLES.contains(role.toUpperCase());
    }
    
    /**
     * Validate if a role can be assigned during user creation
     */
    public static boolean isCreatableRole(String role) {
        return role != null && CREATABLE_ROLES.contains(role.toUpperCase());
    }
    
    /**
     * Get default role for new users
     */
    public static String getDefaultRole() {
        return DEFAULT_ROLE;
    }
    
    /**
     * Validate and sanitize role for creation
     * Returns default role if invalid role provided
     */
    public static String validateRoleForCreation(String role) {
        if (role == null || role.trim().isEmpty()) {
            return DEFAULT_ROLE;
        }
        
        String upperRole = role.trim().toUpperCase();
        
        // If trying to create protected role, default to USER
        if (PROTECTED_ROLES.contains(upperRole)) {
            return DEFAULT_ROLE;
        }
        
        // If valid creatable role, return it
        if (CREATABLE_ROLES.contains(upperRole)) {
            return upperRole;
        }
        
        // Otherwise default to USER
        return DEFAULT_ROLE;
    }
    
    /**
     * Check if a user can be modified (not a protected role)
     */
    public static boolean canModifyUser(String role) {
        return !isProtectedRole(role);
    }
    
    /**
     * Check if a user can be deleted (not a protected role)
     */
    public static boolean canDeleteUser(String role) {
        return !isProtectedRole(role);
    }
}

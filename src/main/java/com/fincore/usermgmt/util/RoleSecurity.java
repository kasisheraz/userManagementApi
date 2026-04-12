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
        "SUPER_ADMIN", 
        "SYSTEM_ADMINISTRATOR"
    );
    
    /**
     * Check if a role is protected from modification
     */
    public static boolean isProtectedRole(String role) {
        return role != null && PROTECTED_ROLES.contains(role.toUpperCase());
    }
    
    /**
     * Validate and sanitize role for creation
     * Returns null if trying to create protected role, otherwise returns the role as-is
     * 
     * @param role - The role name to validate
     * @return The role name if valid, null if protected role attempted
     */
    public static String validateRoleForCreation(String role) {
        if (role == null || role.trim().isEmpty()) {
            return null;
        }
        
        // Prevent creation of protected roles (SUPER_ADMIN, SYSTEM_ADMINISTRATOR)
        if (isProtectedRole(role)) {
            throw new IllegalArgumentException("Cannot create user with protected role: " + role);
        }
        
        // Return the role as-is - let the database determine if it's valid
        return role.trim();
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

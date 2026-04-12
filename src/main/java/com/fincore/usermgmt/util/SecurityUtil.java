package com.fincore.usermgmt.util;

import com.fincore.usermgmt.entity.User;
import com.fincore.usermgmt.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Utility class for accessing security context and current user information.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SecurityUtil {

    private final UserRepository userRepository;

    /**
     * Get the currently authenticated user's phone number from the security context.
     * 
     * @return The phone number of the authenticated user, or null if not authenticated
     */
    public String getCurrentUserPhoneNumber() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            
            // Handle UserDetails principal (created by JWT filter)
            if (principal instanceof UserDetails) {
                String phoneNumber = ((UserDetails) principal).getUsername();
                log.debug("Extracted phone number from UserDetails: {}", phoneNumber);
                return phoneNumber;
            }
            
            // Handle String principal (fallback)
            if (principal instanceof String) {
                log.debug("Extracted phone number from String principal: {}", principal);
                return (String) principal;
            }
            
            log.warn("Unknown principal type: {}", principal.getClass().getName());
        }
        return null;
    }

    /**
     * Get the currently authenticated user from the database.
     * 
     * @return Optional containing the current user if found
     */
    public Optional<User> getCurrentUser() {
        String phoneNumber = getCurrentUserPhoneNumber();
        if (phoneNumber != null) {
            return userRepository.findByPhoneNumber(phoneNumber);
        }
        return Optional.empty();
    }

    /**
     * Check if the current user has a specific role.
     * 
     * @param roleName The role name to check
     * @return true if the user has the specified role
     */
    public boolean hasRole(String roleName) {
        return getCurrentUser()
                .map(user -> user.getRole() != null && roleName.equals(user.getRole().getName()))
                .orElse(false);
    }

    /**
     * Check if the current user is a Business User (restricted access).
     * 
     * @return true if the current user has the "Business User" role
     */
    public boolean isBusinessUser() {
        return hasRole("Business User");
    }

    /**
     * Check if the current user can see all data based on their role.
     * Business Users can only see their own data.
     * 
     * @return true if the user can see all data
     */
    public boolean canSeeAllData() {
        return !isBusinessUser();
    }
}

package com.fincore.usermgmt.dto;

import com.fincore.usermgmt.entity.UserStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserDTO {
    private Long id;
    private String username;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String employeeId;
    private String department;
    private String jobTitle;
    private UserStatus status;
    private String role;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

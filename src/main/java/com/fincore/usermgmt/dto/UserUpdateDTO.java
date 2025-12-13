package com.fincore.usermgmt.dto;

import com.fincore.usermgmt.entity.UserStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserUpdateDTO {
    @Size(min = 3, max = 50)
    private String username;

    @Size(min = 8, max = 100)
    private String password;

    @Size(max = 100)
    private String fullName;

    @Email
    @Size(max = 100)
    private String email;

    private String phoneNumber;
    private String employeeId;
    private String department;
    private String jobTitle;
    private UserStatus status;
    private String role;
}

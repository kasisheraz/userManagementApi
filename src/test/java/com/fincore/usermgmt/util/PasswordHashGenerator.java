package com.fincore.usermgmt.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordHashGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        System.out.println("Admin@123456: " + encoder.encode("Admin@123456"));
        System.out.println("Compliance@123: " + encoder.encode("Compliance@123"));
        System.out.println("Staff@123456: " + encoder.encode("Staff@123456"));
    }
}

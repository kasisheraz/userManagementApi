package com.fincore.usermgmt.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "permissions")
@Data
public class Permission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Permission_Identifier")
    private Long id;

    @Column(name = "Permission_Name", unique = true, nullable = false, length = 100)
    private String name;

    @Column(name = "Description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "Resource", length = 50)
    private String resource;

    @Column(name = "Action", length = 50)
    private String action;

    @Column(name = "Created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}

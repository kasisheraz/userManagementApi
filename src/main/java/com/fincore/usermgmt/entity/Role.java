package com.fincore.usermgmt.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "roles")
@Data
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Role_Identifier")
    private Long id;

    @Column(name = "Role_Name", length = 30)
    private String name;

    @Column(name = "Role_Description", length = 100)
    private String description;

    @Column(name = "Created_Datetime")
    private LocalDateTime createdDatetime;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "Role_Permissions",
        joinColumns = @JoinColumn(name = "Role_Identifier"),
        inverseJoinColumns = @JoinColumn(name = "Permission_Identifier")
    )
    private Set<Permission> permissions;

    @PrePersist
    protected void onCreate() {
        createdDatetime = LocalDateTime.now();
    }
}

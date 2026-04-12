package com.fincore.usermgmt.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for Role information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Role information")
public class RoleDTO {
    
    @Schema(description = "Role identifier", example = "1")
    private Integer id;
    
    @Schema(description = "Role name", example = "Admin")
    private String name;
    
    @Schema(description = "Role description", example = "Administrator with full system access")
    private String description;
}

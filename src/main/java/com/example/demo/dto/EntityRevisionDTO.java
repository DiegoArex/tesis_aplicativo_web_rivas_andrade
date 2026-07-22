package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para información de revisión de entidades (Envers)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EntityRevisionDTO {
    private Number revisionNumber;
    private Long timestamp;
    private String username;
    private String ipAddress;
    private String revisionType; // ADD, MOD, DEL
    private Object entityData;
}

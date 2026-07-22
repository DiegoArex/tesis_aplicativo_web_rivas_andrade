package com.example.demo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para crear/actualizar Usuario
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioRequestDTO {

    @NotBlank(message = "El username es obligatorio")
    private String username;

    @Email(message = "El email debe ser válido")
    private String email;

    @NotBlank(message = "El nombre completo es obligatorio")
    private String nombreCompleto;

    private String keycloakId;

    private String roles; // Separados por coma

    private String carrera;

    private String facultad;

    private Boolean activo = true;
}

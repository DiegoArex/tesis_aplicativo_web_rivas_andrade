package com.example.demo.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.envers.Audited;

import java.time.LocalDateTime;

@Entity
@Table(name = "usuarios", indexes = {
        @Index(name = "idx_usuario_activo", columnList = "activo"),
        @Index(name = "idx_usuario_carrera", columnList = "carrera"),
        @Index(name = "idx_usuario_facultad", columnList = "facultad"),
        @Index(name = "idx_usuario_created_at", columnList = "createdAt"),
        @Index(name = "idx_usuario_deleted_at", columnList = "deletedAt")
})
@Audited
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = true)
    private String keycloakId; // UUID del usuario en Keycloak (opcional para usuarios locales)

    @NotBlank
    @Column(unique = true, nullable = false, length = 100)
    private String username;

    @Email
    @Column(length = 150)
    private String email;

    @NotBlank
    @Column(nullable = false, length = 200)
    private String nombreCompleto;

    @Column(length = 500)
    private String roles; // Roles separados por coma (ADMIN, PROFESOR, DIRECTOR)

    @Column(length = 100)
    private String carrera; // Para directores de carrera

    @Column(length = 100)
    private String facultad; // Para directores de facultad

    @Column(nullable = false)
    private Boolean activo = true;

    /**
     * Soft delete: fecha y hora en que se eliminó el usuario
     * NULL = usuario activo, NOT NULL = usuario eliminado
     */
    @Column
    private LocalDateTime deletedAt;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // =======Metodos Helper para Soft Delete, se le aumento para las auditorias

    // PARA VER SI EL USUARIO esta eliminado
    public boolean isDeleted() {
        return deletedAt != null;
    }

    // Marcar el usuario como eliminado
    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
        this.activo = false;
    }

    // RESTAURAR un usuario Eliminado
    public void restore() {
        this.deletedAt = null;
        this.activo = true;
    }
}

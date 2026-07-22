package com.example.demo.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// laboratorio de computación 1,2,3,4,5,6,7 a,b....../

@Entity
@Table(name = "laboratorios", indexes = {
        @Index(name = "idx_laboratorio_activo", columnList = "activo"),
        @Index(name = "idx_laboratorio_created_at", columnList = "createdAt"),
        @Index(name = "idx_laboratorio_deleted_at", columnList = "deletedAt")
})
@Audited
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Laboratorio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(unique = true, nullable = false, length = 100)
    private String nombre;

    @NotBlank
    @Column(nullable = false, length = 200)
    private String ubicacion;

    @Min(1)
    @Column(nullable = false)
    private Integer capacidad; // Número de equipos en el laboratorio

    @Column(length = 500)
    private String descripcion;

    @Column(nullable = false)
    private Boolean activo = true;

    /**
     * Soft delete: fecha y hora en que se eliminó el laboratorio
     * NULL = laboratorio activo, NOT NULL = laboratorio eliminado
     */
    @Column
    private LocalDateTime deletedAt;

    @NotAudited
    @OneToMany(mappedBy = "laboratorio", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Equipo> equipos = new ArrayList<>();

    @NotAudited
    @OneToMany(mappedBy = "laboratorio", cascade = CascadeType.ALL)
    private List<RegistroUso> registrosUso = new ArrayList<>();

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // ==================== Métodos Helper para Soft Delete ====================

    /**
     * Verifica si el laboratorio está eliminado (soft delete)
     */
    public boolean isDeleted() {
        return deletedAt != null;
    }

    /**
     * Marca el laboratorio como eliminado (soft delete)
     */
    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
        this.activo = false;
    }

    /**
     * Restaura un laboratorio eliminado
     */
    public void restore() {
        this.deletedAt = null;
        this.activo = true;
    }
}

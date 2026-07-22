package com.example.demo.entity;

import jakarta.persistence.*;
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

@Entity
@Table(name = "equipos", indexes = {
        @Index(name = "idx_equipo_estado", columnList = "estado"),
        @Index(name = "idx_equipo_tipo", columnList = "tipo"),
        @Index(name = "idx_equipo_laboratorio", columnList = "laboratorio_id"),
        @Index(name = "idx_equipo_created_at", columnList = "createdAt"),
        @Index(name = "idx_equipo_deleted_at", columnList = "deletedAt")
})
@Audited
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Equipo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(unique = true, nullable = false, length = 50)
    private String codigo; // Ej: "LAB1-PC01"

    @NotBlank
    @Column(nullable = false, length = 50)
    private String tipo; // Ej: "PC", "Laptop", "Proyector", "pantalla"

    @Column(length = 100)
    private String marca;

    @Column(length = 100)
    private String modelo;

    @Column(length = 100)
    private String numeroSerie;

    @NotBlank
    @Column(nullable = false, length = 50)
    private String estado; // "OPERATIVO", "EN_MANTENIMIENTO", "DAÑADO", "FUERA_DE_SERVICIO"

    @NotAudited
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "laboratorio_id", nullable = false)
    private Laboratorio laboratorio;

    // Soft delete: fecha y hora en que se eliminó el equipo
    // NULL = equipo activo, NOT NULL = equipo eliminado

    @Column
    private LocalDateTime deletedAt;

    @NotAudited
    @OneToMany(mappedBy = "equipo", cascade = CascadeType.ALL)
    private List<Novedad> novedades = new ArrayList<>();

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // Si esta eliminado
    public boolean isDeleted() {
        return deletedAt != null;
    }

    // Se lo pone como eliminado
    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }

    // restaurar
    public void restore() {
        this.deletedAt = null;
    }
}

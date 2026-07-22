package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Esta clase se mantiene para compatibilidad pero NO es usada.
 * Se prefiere CustomRevisionEntity que ya captura usuario e IP automáticamente.
 * (Depositado aquí por si necesita referencias en el futuro)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditoriaRevisionEntity {

    private int id;
    private long timestamp;
    private String usuario;
    private String ipAddress;
}

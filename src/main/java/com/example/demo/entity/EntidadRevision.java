package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.envers.RevisionEntity;
import org.hibernate.envers.RevisionNumber;
import org.hibernate.envers.RevisionTimestamp;

//Entidad de Revisión Personalizada para Hibernate Envers
//Almacena información adicional sobre quién y desde dónde se realizó cada
//cambio

@Entity
@Table(name = "REVINFO")
@RevisionEntity(com.example.demo.config.CustomRevisionListener.class)
@Data
public class EntidadRevision {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @RevisionNumber
    private Long id;

    @RevisionTimestamp
    private Long timestamp;

    @Column(length = 100)
    private String username;

    @Column(length = 50)
    private String ipAddress;
}

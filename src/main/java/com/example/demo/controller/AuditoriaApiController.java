package com.example.demo.controller;

import com.example.demo.dto.AuditoriaDTO;
import com.example.demo.entity.EntidadRevision;
import com.example.demo.entity.Equipo;
import com.example.demo.entity.Laboratorio;
import com.example.demo.entity.Novedad;
import com.example.demo.entity.Usuario;
import com.example.demo.security.PermissionConstants;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.RevisionType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * API REST para auditoria consumida por Android.
 * Base URL: /api/auditoria
 */
@RestController
@RequestMapping("/api/auditoria")
@RequiredArgsConstructor
public class AuditoriaApiController {

    private final EntityManager entityManager;

    /**
     * GET /api/auditoria/recent
     * Devuelve auditoria consolidada desde Envers para Usuario, Equipo, Laboratorio y Novedad.
     */
    @GetMapping("/recent")
    @PreAuthorize(PermissionConstants.DIRECTORES)
    public List<AuditoriaDTO> getRecentAuditoria(@RequestParam(defaultValue = "100") int limit) {
        List<AuditoriaDTO> registros = new ArrayList<>();

        try {
            AuditReader auditReader = AuditReaderFactory.get(entityManager);

            agregarAuditoriaSegura(registros, auditReader, Usuario.class, "Usuario");
            agregarAuditoriaSegura(registros, auditReader, Equipo.class, "Equipo");
            agregarAuditoriaSegura(registros, auditReader, Laboratorio.class, "Laboratorio");
            agregarAuditoriaSegura(registros, auditReader, Novedad.class, "Novedad");

            registros.sort((a, b) -> {
                if (a.getFechaCambio() == null || b.getFechaCambio() == null) {
                    return 0;
                }
                return b.getFechaCambio().compareTo(a.getFechaCambio());
            });

            if (limit > 0 && registros.size() > limit) {
                return new ArrayList<>(registros.subList(0, limit));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return registros;
    }

    private void agregarAuditoriaSegura(List<AuditoriaDTO> destino, AuditReader auditReader,
                                        Class<?> entityClass, String nombreEntidad) {
        try {
            destino.addAll(obtenerAuditoriaEntidad(auditReader, entityClass, nombreEntidad));
        } catch (RuntimeException ex) {
            ex.printStackTrace();
        }
    }

    private List<AuditoriaDTO> obtenerAuditoriaEntidad(AuditReader auditReader, Class<?> entityClass, String nombreEntidad) {
        List<AuditoriaDTO> result = new ArrayList<>();

        try {
            List<Object[]> auditData = auditReader.createQuery()
                    .forRevisionsOfEntity(entityClass, false, true)
                    .getResultList();

            for (Object[] row : auditData) {
                if (row.length < 3) {
                    continue;
                }

                AuditoriaDTO.AuditoriaDTOBuilder dtoBuilder = AuditoriaDTO.builder();
                dtoBuilder.tipoEntidad(nombreEntidad);

                Object entity = row[0];
                if (entity instanceof Usuario usuario) {
                    dtoBuilder.idEntidad(usuario.getId());
                    dtoBuilder.nombreEntidad(usuario.getNombreCompleto() + " (" + usuario.getUsername() + ")");
                    dtoBuilder.detalles("Email: " + usuario.getEmail() + " | Roles: " + usuario.getRoles());
                } else if (entity instanceof Equipo equipo) {
                    dtoBuilder.idEntidad(equipo.getId());
                    dtoBuilder.nombreEntidad(equipo.getCodigo() + " - " + equipo.getModelo());
                    dtoBuilder.detalles("Tipo: " + equipo.getTipo() + " | Estado: " + equipo.getEstado());
                } else if (entity instanceof Laboratorio laboratorio) {
                    dtoBuilder.idEntidad(laboratorio.getId());
                    dtoBuilder.nombreEntidad(laboratorio.getNombre());
                    dtoBuilder.detalles("Ubicacion: " + laboratorio.getUbicacion() + " | Cap: " + laboratorio.getCapacidad());
                } else if (entity instanceof Novedad novedad) {
                    dtoBuilder.idEntidad(novedad.getId());
                    dtoBuilder.nombreEntidad(novedad.getTitulo());
                    dtoBuilder.detalles("Estado: " + novedad.getEstado() + " | Tipo: " + novedad.getTipo());
                }

                if (row[1] instanceof EntidadRevision revision) {
                    dtoBuilder.revisionId(revision.getId());
                    dtoBuilder.usuario(revision.getUsername() != null ? revision.getUsername() : "SISTEMA");
                    dtoBuilder.ipAddress(revision.getIpAddress() != null ? revision.getIpAddress() : "N/A");

                    Date fecha = new Date(revision.getTimestamp());
                    dtoBuilder.fechaCambio(LocalDateTime.ofInstant(fecha.toInstant(), ZoneId.systemDefault()));
                }

                if (row[2] instanceof RevisionType revType) {
                    String accion = revType.toString();
                    dtoBuilder.accion(accion);

                    String descripcion = "ADD".equals(accion) ? "Creado"
                            : "MOD".equals(accion) ? "Modificado"
                            : "DEL".equals(accion) ? "Eliminado"
                            : accion;
                    dtoBuilder.descripcionAccion(descripcion);
                }

                result.add(dtoBuilder.build());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }
}

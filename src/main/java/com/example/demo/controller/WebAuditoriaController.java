package com.example.demo.controller;

import com.example.demo.dto.AuditoriaDTO;
import com.example.demo.dto.ActividadUsuarioDTO;
import com.example.demo.dto.AuditoriaUsoLaboratorioDTO;
import com.example.demo.entity.EntidadRevision;
import com.example.demo.entity.Equipo;
import com.example.demo.entity.Laboratorio;
import com.example.demo.entity.Novedad;
import com.example.demo.entity.RegistroUso;
import com.example.demo.entity.Usuario;
import com.example.demo.repository.RegistroUsoRepository;
import com.example.demo.repository.NovedadRepository;
import com.example.demo.security.PermissionConstants;
import com.example.demo.service.AuditoriaService;
import lombok.RequiredArgsConstructor;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.RevisionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import jakarta.persistence.EntityManager;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Date;

/**
 * Controlador Web para Auditoría con Thymeleaf
 * Base URL: /web/auditoria
 */
@Controller
@RequestMapping("/web/auditoria")
@RequiredArgsConstructor
public class WebAuditoriaController {

    private static final Logger logger = LoggerFactory.getLogger(WebAuditoriaController.class);

    private final EntityManager entityManager;
    private final AuditoriaService auditoriaService;
    private final RegistroUsoRepository registroUsoRepository;
    private final NovedadRepository novedadRepository;

    // GET /web/auditoria - Página principal de auditoría
    @GetMapping
    @PreAuthorize(PermissionConstants.DIRECTORES)
    public String indexAuditoria() {
        return "auditoria/index";
    }

    // GET /web/auditoria/lista - Ver historial de cambios
    @GetMapping("/lista")
    @PreAuthorize(PermissionConstants.DIRECTORES)
    @Transactional(readOnly = true)
    public String verAuditoria(Model model) {
        List<AuditoriaDTO> registros = new ArrayList<>();
        
        try {
            AuditReader auditReader = AuditReaderFactory.get(entityManager);
            
            // Obtener las revisiones de cada entidad auditada
            registros.addAll(obtenerAuditoriaEntidad(auditReader, Usuario.class, "Usuario"));
            registros.addAll(obtenerAuditoriaEntidad(auditReader, Equipo.class, "Equipo"));
            registros.addAll(obtenerAuditoriaEntidad(auditReader, Laboratorio.class, "Laboratorio"));
            registros.addAll(obtenerAuditoriaEntidad(auditReader, Novedad.class, "Novedad"));
            
            // Ordenar por fecha descendente (más recientes primero)
            Collections.sort(registros, (a, b) -> {
                if (a.getFechaCambio() == null || b.getFechaCambio() == null) return 0;
                return b.getFechaCambio().compareTo(a.getFechaCambio());
            });
            
        } catch (Exception e) {
            logger.error("[Auditoria] Error inesperado", e);
        }
        
        model.addAttribute("titulo", "Auditoría del Sistema");
        model.addAttribute("registros", registros);
        return "auditoria/lista";
    }

    /**
     * Obtiene los registros de auditoría para una entidad específica
     * Captura nombre, descripción y cambios específicos de la entidad
     */
    @SuppressWarnings("unchecked")
    private List<AuditoriaDTO> obtenerAuditoriaEntidad(AuditReader auditReader, Class<?> entityClass, String nombreEntidad) {
        List<AuditoriaDTO> result = new ArrayList<>();
        
        logger.info("[Auditoria] [{}] Iniciando obtención de auditoría", nombreEntidad);
        
        try {
            List<Object[]> auditData = auditReader.createQuery()
                    .forRevisionsOfEntity(entityClass, false, true)
                    .getResultList();
            
            logger.info("[Auditoria] [{}] Total de registros en BD: {}", nombreEntidad, auditData.size());
            
            for (int idx = 0; idx < auditData.size(); idx++) {
                Object[] row = auditData.get(idx);
                
                try {
                    if (row == null || row.length < 3) {
                        logger.warn("[Auditoria] [{}] Fila {} con longitud inválida: {}", nombreEntidad, idx, row != null ? row.length : "null");
                        continue;
                    }
                    
                    AuditoriaDTO.AuditoriaDTOBuilder dtoBuilder = AuditoriaDTO.builder();
                    dtoBuilder.tipoEntidad(nombreEntidad);
                    
                    // El objeto auditado está en row[0]
                    Object entity = row[0];
                    
                    if (entity != null) {
                        if (entity instanceof Equipo) {
                            Equipo equipo = (Equipo) entity;
                            dtoBuilder.idEntidad(equipo.getId());
                            dtoBuilder.nombreEntidad(equipo.getCodigo() + " - " + equipo.getModelo());
                            dtoBuilder.detalles("Tipo: " + equipo.getTipo() + " | Estado: " + equipo.getEstado());
                            logger.info("[Auditoria] [EQUIPO] ID={}, Código={}, Modelo={}", equipo.getId(), equipo.getCodigo(), equipo.getModelo());
                        } else if (entity instanceof Laboratorio) {
                            Laboratorio laboratorio = (Laboratorio) entity;
                            dtoBuilder.idEntidad(laboratorio.getId());
                            dtoBuilder.nombreEntidad(laboratorio.getNombre());
                            dtoBuilder.detalles("Ubicación: " + laboratorio.getUbicacion() + " | Cap: " + laboratorio.getCapacidad());
                        } else if (entity instanceof Novedad) {
                            Novedad novedad = (Novedad) entity;
                            dtoBuilder.idEntidad(novedad.getId());
                            dtoBuilder.nombreEntidad(novedad.getTitulo());
                            dtoBuilder.detalles("Estado: " + novedad.getEstado() + " | Tipo: " + novedad.getTipo());
                        } else if (entity instanceof Usuario) {
                            Usuario usuario = (Usuario) entity;
                            dtoBuilder.idEntidad(usuario.getId());
                            dtoBuilder.nombreEntidad(usuario.getNombreCompleto() + " (" + usuario.getUsername() + ")");
                            dtoBuilder.detalles("Email: " + usuario.getEmail() + " | Roles: " + usuario.getRoles());
                        }
                    }
                    
                    // row[1] es la revisión (CustomRevisionEntity)
                    if (row[1] != null) {
                        EntidadRevision revision = (EntidadRevision) row[1];
                        dtoBuilder.revisionId(revision.getId());
                        dtoBuilder.usuario(revision.getUsername() != null ? revision.getUsername() : "SISTEMA");
                        dtoBuilder.ipAddress(revision.getIpAddress() != null ? revision.getIpAddress() : "N/A");
                        
                        // Convertir timestamp a LocalDateTime
                        Date fecha = new Date(revision.getTimestamp());
                        dtoBuilder.fechaCambio(LocalDateTime.ofInstant(
                            fecha.toInstant(), 
                            ZoneId.systemDefault()
                        ));
                    }
                    
                    // row[2] es el tipo de revisión (RevisionType)
                    if (row[2] != null) {
                        RevisionType revType = (RevisionType) row[2];
                        String accion = revType.toString();
                        dtoBuilder.accion(accion);
                        
                        // Traducción legible
                        String descripcion = "ADD".equals(accion) ? "Creado" : 
                                           "MOD".equals(accion) ? "Modificado" : 
                                           "DEL".equals(accion) ? "Eliminado" : accion;
                        dtoBuilder.descripcionAccion(descripcion);
                    }
                    
                    AuditoriaDTO dto = dtoBuilder.build();
                    result.add(dto);
                    
                } catch (Exception e) {
                    logger.error("[Auditoria] [{}] Error procesando fila {}", nombreEntidad, idx, e);
                }
            }
            
            logger.info("[Auditoria] [{}] Procesados {} registros exitosamente", nombreEntidad, result.size());
            
        } catch (Exception e) {
            logger.error("[Auditoria] [{}] Error general en obtenerAuditoriaEntidad", nombreEntidad, e);
        }
        
        return result;
    }

    /**
     * GET /web/auditoria/descargar - Descargar auditoría en Excel
     */
    @GetMapping("/descargar")
    @PreAuthorize(PermissionConstants.DIRECTORES)
    public ResponseEntity<byte[]> descargarExcel() {
        try {
            List<AuditoriaDTO> registros = new ArrayList<>();
            
            AuditReader auditReader = AuditReaderFactory.get(entityManager);
            
            // Obtener las revisiones de cada entidad auditada
            registros.addAll(obtenerAuditoriaEntidad(auditReader, Usuario.class, "Usuario"));
            registros.addAll(obtenerAuditoriaEntidad(auditReader, Equipo.class, "Equipo"));
            registros.addAll(obtenerAuditoriaEntidad(auditReader, Laboratorio.class, "Laboratorio"));
            registros.addAll(obtenerAuditoriaEntidad(auditReader, Novedad.class, "Novedad"));
            
            // Ordenar por fecha descendente
            Collections.sort(registros, (a, b) -> {
                if (a.getFechaCambio() == null || b.getFechaCambio() == null) return 0;
                return b.getFechaCambio().compareTo(a.getFechaCambio());
            });
            
            // Generar Excel
            byte[] excelBytes = auditoriaService.exportarAExcel(registros);
            
            // Preparar respuesta
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.valueOf("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.setContentDispositionFormData("attachment", "auditoria_" + System.currentTimeMillis() + ".xlsx");
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(excelBytes);
                    
        } catch (Exception e) {
            logger.error("[Auditoria] Error inesperado", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * GET /web/auditoria/uso-laboratorios - Ver auditoría de uso de laboratorios
     * Muestra: qué sala reservó, cuándo entró/salió, qué clase dio, novedades reportadas
     * OPTIMIZADO: Usa JOIN FETCH para cargar relaciones de una vez, evita N+1 queries
     */
    @GetMapping("/uso-laboratorios")
    @PreAuthorize(PermissionConstants.DIRECTORES)
    @Transactional(readOnly = true)
    public String verAuditoriaUsoLaboratorios(Model model) {
        List<AuditoriaUsoLaboratorioDTO> auditoriaUsos = new ArrayList<>();
        
        try {
            // ✅ OPTIMIZADO: Cargar registros con todas las relaciones en una sola query
            // Antes: 1 query para registros + N queries para usuarios + N queries para laboratorios
            // Ahora: 1 query con JOIN FETCH
            List<RegistroUso> registros = registroUsoRepository.findAllWithRelations();
            
            // ✅ OPTIMIZADO: Cargar todas las novedades de una sola vez en lugar de un query por usuario
            List<Novedad> todasLasNovedades = novedadRepository.findAllWithRelationsUnpaged();
            
            // Crear un mapa: usuarioId -> lista de novedades para acceso O(1)
            Map<Long, List<Novedad>> novedadesPorUsuario = todasLasNovedades.stream()
                    .collect(Collectors.groupingBy(n -> n.getUsuarioReporta().getId()));
            
            for (RegistroUso registro : registros) {
                AuditoriaUsoLaboratorioDTO dto = AuditoriaUsoLaboratorioDTO.builder()
                        // Usuario - ya cargado con JOIN FETCH, sin queries adicionales
                        .usuarioId(registro.getUsuario().getId())
                        .usuario(registro.getUsuario().getUsername())
                        .nombreCompletoUsuario(registro.getUsuario().getNombreCompleto())
                        
                        // Laboratorio/Sala Principal - ya cargado con JOIN FETCH
                        .laboratorioId(registro.getLaboratorio().getId())
                        .nombreLaboratorio(registro.getLaboratorio().getNombre())
                        .ubicacionLaboratorio(registro.getLaboratorio().getUbicacion())
                        
                        // Laboratorio Secundario - cargado con LEFT JOIN FETCH
                        .laboratorioSecundarioId(registro.getLaboratorioSecundario() != null ? 
                            registro.getLaboratorioSecundario().getId() : null)
                        .nombreLaboratorioSecundario(registro.getLaboratorioSecundario() != null ? 
                            registro.getLaboratorioSecundario().getNombre() : null)
                        .ubicacionLaboratorioSecundario(registro.getLaboratorioSecundario() != null ? 
                            registro.getLaboratorioSecundario().getUbicacion() : null)
                        
                        // Tipo de Registro
                        .tipoRegistro(registro.getTipoRegistro() != null ? 
                            registro.getTipoRegistro().getNombre() : "Normal")
                        .esExamen(registro.getLaboratorioSecundario() != null)
                        
                        // Información de Uso
                        .horaEntrada(registro.getFechaEntrada())
                        .horaSalida(registro.getFechaSalida())
                        .proposito(registro.getProposito())
                        .observaciones(registro.getObservaciones())
                        .build();
                
                // Calcular duración si ya cerró
                if (registro.getFechaSalida() != null) {
                    long duracionMinutos = ChronoUnit.MINUTES.between(
                            registro.getFechaEntrada(), 
                            registro.getFechaSalida());
                    dto.setDuracionMinutos(duracionMinutos);
                    
                    // Formatear duración
                    long horas = duracionMinutos / 60;
                    long minutos = duracionMinutos % 60;
                    dto.setDuracionFormatada(horas + "h " + minutos + "min");
                }
                
                // ✅ OPTIMIZADO: Buscar novedades en el mapa cacheado en memoria
                // Antes: Un findByUsuarioReportaId() por cada registro
                // Ahora: O(1) lookup en el mapa después de cargar TODO una sola vez
                Long usuarioId = registro.getUsuario().getId();
                List<Novedad> novedadesUsuario = novedadesPorUsuario.getOrDefault(usuarioId, Collections.emptyList());
                
                // Buscar novedad que coincida con el rango de tiempo de la sesión
                for (Novedad novedad : novedadesUsuario) {
                    if (novedad.getFechaReporte().isAfter(registro.getFechaEntrada())) {
                        LocalDateTime fechaSalida = registro.getFechaSalida() != null ? 
                            registro.getFechaSalida() : LocalDateTime.now();
                        
                        if (novedad.getFechaReporte().isBefore(fechaSalida)) {
                            dto.setNovedadId(novedad.getId());
                            dto.setNombreNovedad(novedad.getTitulo());
                            dto.setTipoNovedad(novedad.getTipo().toString());
                            dto.setEstadoNovedad(novedad.getEstado().toString());
                            break;  // Una novedad por sesión
                        }
                    }
                }
                
                auditoriaUsos.add(dto);
            }
            
            // Ordenar por fecha de entrada descendente
            Collections.sort(auditoriaUsos, (a, b) -> {
                if (a.getHoraEntrada() == null || b.getHoraEntrada() == null) return 0;
                return b.getHoraEntrada().compareTo(a.getHoraEntrada());
            });
            
        } catch (Exception e) {
            logger.error("[Auditoria] Error inesperado", e);
        }
        
        model.addAttribute("titulo", "Auditoría de Uso de Laboratorios");
        model.addAttribute("auditoriaUsos", auditoriaUsos);
        return "auditoria/uso-laboratorios";
    }

    /**
     * GET /web/auditoria/uso-laboratorios/descargar - Descargar auditoría de uso de laboratorios en Excel
     * OPTIMIZADO: Usa JOIN FETCH para evitar N+1 queries
     */
    @GetMapping("/uso-laboratorios/descargar")
    @PreAuthorize(PermissionConstants.DIRECTORES)
    public ResponseEntity<byte[]> descargarAuditoriaUsoLaboratorios() {
        try {
            List<AuditoriaUsoLaboratorioDTO> auditoriaUsos = new ArrayList<>();
            
            // ✅ OPTIMIZADO: Cargar registros con todas las relaciones en una sola query
            List<RegistroUso> registros = registroUsoRepository.findAllWithRelations();
            
            // ✅ OPTIMIZADO: Cargar todas las novedades de una sola vez
            List<Novedad> todasLasNovedades = novedadRepository.findAllWithRelationsUnpaged();
            
            // Crear un mapa para acceso O(1)
            Map<Long, List<Novedad>> novedadesPorUsuario = todasLasNovedades.stream()
                    .collect(Collectors.groupingBy(n -> n.getUsuarioReporta().getId()));
            
            for (RegistroUso registro : registros) {
                AuditoriaUsoLaboratorioDTO dto = AuditoriaUsoLaboratorioDTO.builder()
                        // Usuario - ya cargado con JOIN FETCH
                        .usuarioId(registro.getUsuario().getId())
                        .usuario(registro.getUsuario().getUsername())
                        .nombreCompletoUsuario(registro.getUsuario().getNombreCompleto())
                        
                        // Laboratorio/Sala Principal - ya cargado con JOIN FETCH
                        .laboratorioId(registro.getLaboratorio().getId())
                        .nombreLaboratorio(registro.getLaboratorio().getNombre())
                        .ubicacionLaboratorio(registro.getLaboratorio().getUbicacion())
                        
                        // Laboratorio Secundario - cargado con LEFT JOIN FETCH
                        .laboratorioSecundarioId(registro.getLaboratorioSecundario() != null ? 
                            registro.getLaboratorioSecundario().getId() : null)
                        .nombreLaboratorioSecundario(registro.getLaboratorioSecundario() != null ? 
                            registro.getLaboratorioSecundario().getNombre() : null)
                        .ubicacionLaboratorioSecundario(registro.getLaboratorioSecundario() != null ? 
                            registro.getLaboratorioSecundario().getUbicacion() : null)
                        
                        // Tipo de Registro
                        .tipoRegistro(registro.getTipoRegistro() != null ? 
                            registro.getTipoRegistro().getNombre() : "Normal")
                        .esExamen(registro.getLaboratorioSecundario() != null)
                        
                        // Información de Uso
                        .horaEntrada(registro.getFechaEntrada())
                        .horaSalida(registro.getFechaSalida())
                        .proposito(registro.getProposito())
                        .observaciones(registro.getObservaciones())
                        .build();
                
                // Calcular duración si ya cerró
                if (registro.getFechaSalida() != null) {
                    long duracionMinutos = ChronoUnit.MINUTES.between(
                            registro.getFechaEntrada(), 
                            registro.getFechaSalida());
                    dto.setDuracionMinutos(duracionMinutos);
                    
                    // Formatear duración
                    long horas = duracionMinutos / 60;
                    long minutos = duracionMinutos % 60;
                    dto.setDuracionFormatada(horas + "h " + minutos + "min");
                }
                
                // ✅ OPTIMIZADO: Buscar novedades en el mapa cacheado
                Long usuarioId = registro.getUsuario().getId();
                List<Novedad> novedadesUsuario = novedadesPorUsuario.getOrDefault(usuarioId, Collections.emptyList());
                
                for (Novedad novedad : novedadesUsuario) {
                    if (novedad.getFechaReporte().isAfter(registro.getFechaEntrada())) {
                        LocalDateTime fechaSalida = registro.getFechaSalida() != null ? 
                            registro.getFechaSalida() : LocalDateTime.now();
                        
                        if (novedad.getFechaReporte().isBefore(fechaSalida)) {
                            dto.setNovedadId(novedad.getId());
                            dto.setNombreNovedad(novedad.getTitulo());
                            dto.setTipoNovedad(novedad.getTipo().toString());
                            dto.setEstadoNovedad(novedad.getEstado().toString());
                            break;
                        }
                    }
                }
                
                auditoriaUsos.add(dto);
            }
            
            // Ordenar por fecha de entrada descendente
            Collections.sort(auditoriaUsos, (a, b) -> {
                if (a.getHoraEntrada() == null || b.getHoraEntrada() == null) return 0;
                return b.getHoraEntrada().compareTo(a.getHoraEntrada());
            });
            
            // Generar Excel
            byte[] excelBytes = auditoriaService.exportarUsoLaboratoriosExcel(auditoriaUsos);
            
            // Preparar respuesta
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.valueOf("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.setContentDispositionFormData("attachment", "auditoria_uso_laboratorios_" + System.currentTimeMillis() + ".xlsx");
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(excelBytes);
                    
        } catch (Exception e) {
            logger.error("[Auditoria] Error inesperado", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * GET /web/auditoria/por-usuario - Ver auditoría agrupada por usuario
     * Muestra qué hizo cada usuario: novedades reportadas, laboratorios reservados, cambios realizados
     */
    @GetMapping("/por-usuario")
    @PreAuthorize(PermissionConstants.DIRECTORES)
    public String verAuditoriaPorUsuario(Model model) {
        List<ActividadUsuarioDTO> actividades = new ArrayList<>();
        
        try {
            AuditReader auditReader = AuditReaderFactory.get(entityManager);
            
            // Obtener auditoría de Novedades (reportes)
            List<AuditoriaDTO> auditoriasNovedades = obtenerAuditoriaEntidad(auditReader, Novedad.class, "Novedad");
            for (AuditoriaDTO audit : auditoriasNovedades) {
                if ("ADD".equals(audit.getAccion())) {
                    ActividadUsuarioDTO actividad = ActividadUsuarioDTO.builder()
                            .usuario(audit.getUsuario())
                            .tipoActividad("NOVEDAD_REPORTADA")
                            .descripcionActividad("Reportó novedad")
                            .tipoEntidad("Novedad")
                            .nombreEntidad(audit.getNombreEntidad())
                            .idEntidad(audit.getIdEntidad())
                            .detalles(audit.getDetalles())
                            .fecha(audit.getFechaCambio())
                            .ipAddress(audit.getIpAddress())
                            .revisionId(audit.getRevisionId())
                            .build();
                    actividades.add(actividad);
                }
            }
            
            // Obtener auditoría de Equipos (asociación/reserva)
            List<AuditoriaDTO> auditoriasEquipos = obtenerAuditoriaEntidad(auditReader, Equipo.class, "Equipo");
            for (AuditoriaDTO audit : auditoriasEquipos) {
                if (!"DEL".equals(audit.getAccion())) {
                    String tipo = "MOD".equals(audit.getAccion()) ? "EQUIPO_MODIFICADO" : "EQUIPO_CREADO";
                    String desc = "MOD".equals(audit.getAccion()) ? "Modificó equipo" : "Creó equipo";
                    
                    ActividadUsuarioDTO actividad = ActividadUsuarioDTO.builder()
                            .usuario(audit.getUsuario())
                            .tipoActividad(tipo)
                            .descripcionActividad(desc)
                            .tipoEntidad("Equipo")
                            .nombreEntidad(audit.getNombreEntidad())
                            .idEntidad(audit.getIdEntidad())
                            .detalles(audit.getDetalles())
                            .fecha(audit.getFechaCambio())
                            .ipAddress(audit.getIpAddress())
                            .revisionId(audit.getRevisionId())
                            .build();
                    actividades.add(actividad);
                }
            }
            
            // Obtener auditoría de Laboratorios (reservas/creación)
            List<AuditoriaDTO> auditoriasLaboratorios = obtenerAuditoriaEntidad(auditReader, Laboratorio.class, "Laboratorio");
            for (AuditoriaDTO audit : auditoriasLaboratorios) {
                String tipo = "MOD".equals(audit.getAccion()) ? "LABORATORIO_RESERVADO" : "LABORATORIO_CREADO";
                String desc = "MOD".equals(audit.getAccion()) ? "Reservó laboratorio" : "Creó laboratorio";
                
                ActividadUsuarioDTO actividad = ActividadUsuarioDTO.builder()
                        .usuario(audit.getUsuario())
                        .tipoActividad(tipo)
                        .descripcionActividad(desc)
                        .tipoEntidad("Laboratorio")
                        .nombreEntidad(audit.getNombreEntidad())
                        .idEntidad(audit.getIdEntidad())
                        .detalles(audit.getDetalles())
                        .fecha(audit.getFechaCambio())
                        .ipAddress(audit.getIpAddress())
                        .revisionId(audit.getRevisionId())
                        .build();
                actividades.add(actividad);
            }
            
            // Obtener auditoría de Usuarios (cambios de estado)
            List<AuditoriaDTO> auditoriasUsuarios = obtenerAuditoriaEntidad(auditReader, Usuario.class, "Usuario");
            for (AuditoriaDTO audit : auditoriasUsuarios) {
                if ("MOD".equals(audit.getAccion())) {
                    ActividadUsuarioDTO actividad = ActividadUsuarioDTO.builder()
                            .usuario(audit.getUsuario())
                            .tipoActividad("USUARIO_MODIFICADO")
                            .descripcionActividad("Modificó usuario")
                            .tipoEntidad("Usuario")
                            .nombreEntidad(audit.getNombreEntidad())
                            .idEntidad(audit.getIdEntidad())
                            .detalles(audit.getDetalles())
                            .fecha(audit.getFechaCambio())
                            .ipAddress(audit.getIpAddress())
                            .revisionId(audit.getRevisionId())
                            .build();
                    actividades.add(actividad);
                }
            }
            
            // Ordenar por fecha descendente
            Collections.sort(actividades, (a, b) -> {
                if (a.getFecha() == null || b.getFecha() == null) return 0;
                return b.getFecha().compareTo(a.getFecha());
            });
            
        } catch (Exception e) {
            logger.error("[Auditoria] Error inesperado", e);
        }
        
        model.addAttribute("titulo", "Auditoría por Usuario");
        model.addAttribute("actividades", actividades);
        return "auditoria/por-usuario";
    }

    /**
     * GET /web/auditoria/por-usuario/descargar - Descargar auditoría por usuario en Excel
     */
    @GetMapping("/por-usuario/descargar")
    @PreAuthorize(PermissionConstants.DIRECTORES)
    public ResponseEntity<byte[]> descargarAuditoriaUsuarioExcel() {
        try {
            List<ActividadUsuarioDTO> actividades = new ArrayList<>();
            
            AuditReader auditReader = AuditReaderFactory.get(entityManager);
            
            // Obtener auditoría de Novedades
            List<AuditoriaDTO> auditoriasNovedades = obtenerAuditoriaEntidad(auditReader, Novedad.class, "Novedad");
            for (AuditoriaDTO audit : auditoriasNovedades) {
                if ("ADD".equals(audit.getAccion())) {
                    ActividadUsuarioDTO actividad = ActividadUsuarioDTO.builder()
                            .usuario(audit.getUsuario())
                            .tipoActividad("NOVEDAD_REPORTADA")
                            .descripcionActividad("Reportó novedad")
                            .tipoEntidad("Novedad")
                            .nombreEntidad(audit.getNombreEntidad())
                            .idEntidad(audit.getIdEntidad())
                            .detalles(audit.getDetalles())
                            .fecha(audit.getFechaCambio())
                            .ipAddress(audit.getIpAddress())
                            .revisionId(audit.getRevisionId())
                            .build();
                    actividades.add(actividad);
                }
            }
            
            // Obtener auditoría de Laboratorios
            List<AuditoriaDTO> auditoriasLaboratorios = obtenerAuditoriaEntidad(auditReader, Laboratorio.class, "Laboratorio");
            for (AuditoriaDTO audit : auditoriasLaboratorios) {
                if (!"DEL".equals(audit.getAccion())) {
                    String desc = "MOD".equals(audit.getAccion()) ? "Reservó laboratorio" : "Creó laboratorio";
                    
                    ActividadUsuarioDTO actividad = ActividadUsuarioDTO.builder()
                            .usuario(audit.getUsuario())
                            .tipoActividad("LABORATORIO_ACTIVIDAD")
                            .descripcionActividad(desc)
                            .tipoEntidad("Laboratorio")
                            .nombreEntidad(audit.getNombreEntidad())
                            .idEntidad(audit.getIdEntidad())
                            .detalles(audit.getDetalles())
                            .fecha(audit.getFechaCambio())
                            .ipAddress(audit.getIpAddress())
                            .revisionId(audit.getRevisionId())
                            .build();
                    actividades.add(actividad);
                }
            }
            
            // Ordenar por fecha descendente
            Collections.sort(actividades, (a, b) -> {
                if (a.getFecha() == null || b.getFecha() == null) return 0;
                return b.getFecha().compareTo(a.getFecha());
            });
            
            // Generar Excel
            byte[] excelBytes = auditoriaService.exportarActividadesUsuarioExcel(actividades);
            
            // Preparar respuesta
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.valueOf("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.setContentDispositionFormData("attachment", "auditoria_usuarios_" + System.currentTimeMillis() + ".xlsx");
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(excelBytes);
                    
        } catch (Exception e) {
            logger.error("[Auditoria] Error inesperado", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * GET /web/auditoria/novedades - Ver auditoría de reportes/novedades
     * Muestra: qué se reportó, cuándo, por quién, estado actual, quién y cuándo lo solucionó
     */
    @GetMapping("/novedades")
    @PreAuthorize(PermissionConstants.DIRECTORES)
    @Transactional(readOnly = true)
    public String verAuditoriaNovedades(Model model) {
        try {
            List<Novedad> novedades = novedadRepository.findAllWithRelationsUnpaged();
            
            // Ordenar por fecha de reporte descendente (más recientes primero)
            Collections.sort(novedades, (a, b) -> {
                if (a.getFechaReporte() == null || b.getFechaReporte() == null) return 0;
                return b.getFechaReporte().compareTo(a.getFechaReporte());
            });
            
            model.addAttribute("novedades", novedades);
        } catch (Exception e) {
            logger.error("[Auditoria] Error inesperado", e);
        }
        
        model.addAttribute("titulo", "Auditoría de Reportes - Novedades");
        return "auditoria/novedades";
    }

    /**
     * GET /web/auditoria/novedades/descargar - Descargar auditoría de novedades en Excel
     */
    @GetMapping("/novedades/descargar")
    @PreAuthorize(PermissionConstants.DIRECTORES)
    public ResponseEntity<byte[]> descargarAuditoriaNovedadesExcel() {
        try {
            // Obtener todas las novedades ordenadas por fecha descendente
            List<Novedad> novedades = novedadRepository.findAll();
            
            // Ordenar por fecha de reporte descendente (más recientes primero)
            Collections.sort(novedades, (a, b) -> {
                if (a.getFechaReporte() == null || b.getFechaReporte() == null) return 0;
                return b.getFechaReporte().compareTo(a.getFechaReporte());
            });
            
            // Generar Excel
            byte[] excelBytes = auditoriaService.exportarNovedadesAExcel(novedades);
            
            // Preparar respuesta
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.valueOf("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.setContentDispositionFormData("attachment", "auditoria_novedades_" + System.currentTimeMillis() + ".xlsx");
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(excelBytes);
                    
        } catch (Exception e) {
            logger.error("[Auditoria] Error inesperado", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * GET /web/auditoria/cambios-operacionales - Ver auditoría de cambios operacionales
     * Muestra: Quién creó, actualizó, eliminó laboratorios y equipos
     * También muestra quién resolvió novedades
     */
    @GetMapping("/cambios-operacionales")
    @PreAuthorize(PermissionConstants.DIRECTORES)
    @Transactional(readOnly = true)
    public String verAuditoriaCambiosOperacionales(Model model) {
        List<AuditoriaDTO> cambios = new ArrayList<>();
        
        try {
            logger.info("[Auditoria] ===== INICIO: /web/auditoria/cambios-operacionales =====");
            
            AuditReader auditReader = null;
            try {
                auditReader = AuditReaderFactory.get(entityManager);
            } catch (Exception e) {
                logger.error("[Auditoria] Error obteniendo AuditReader", e);
                model.addAttribute("titulo", "Auditoría de Cambios Operacionales");
                model.addAttribute("cambios", cambios);
                return "auditoria/cambios-operacionales";
            }

            // Obtener auditoría de Laboratorios
            try {
                List<AuditoriaDTO> laboratorios = obtenerAuditoriaEntidad(auditReader, Laboratorio.class, "Laboratorio");
                cambios.addAll(laboratorios);
                logger.info("[Auditoria] Laboratorios: {}", laboratorios.size());
            } catch (Exception e) {
                logger.error("[Auditoria] Error obteniendo Laboratorios", e);
            }

            // Obtener auditoría de Equipos
            try {
                List<AuditoriaDTO> equipos = obtenerAuditoriaEntidad(auditReader, Equipo.class, "Equipo");
                cambios.addAll(equipos);
                logger.info("[Auditoria] Equipos: {}", equipos.size());
            } catch (Exception e) {
                logger.error("[Auditoria] Error obteniendo Equipos", e);
            }

            // Obtener auditoría de Novedades
            try {
                List<AuditoriaDTO> novedades = obtenerAuditoriaEntidad(auditReader, Novedad.class, "Novedad");
                cambios.addAll(novedades);
                logger.info("[Auditoria] Novedades: {}", novedades.size());
            } catch (Exception e) {
                logger.error("[Auditoria] Error obteniendo Novedades", e);
            }

            // Ordenar por fecha descendente
            try {
                Collections.sort(cambios, (a, b) -> {
                    if (a == null || b == null) return 0;
                    if (a.getFechaCambio() == null || b.getFechaCambio() == null) return 0;
                    return b.getFechaCambio().compareTo(a.getFechaCambio());
                });
                logger.info("[Auditoria] Total registros ordenados: {}", cambios.size());
            } catch (Exception e) {
                logger.error("[Auditoria] Error ordenando registros", e);
            }

        } catch (Exception e) {
            logger.error("[Auditoria] ===== ERROR CRÍTICO =====", e);
            logger.error("[Auditoria] Error inesperado", e);
        }

        logger.info("[Auditoria] Retornando {} registros a la vista", cambios.size());
        
        model.addAttribute("titulo", "Auditoría de Cambios Operacionales");
        model.addAttribute("cambios", cambios);
        
        logger.info("[Auditoria] ===== FIN: Vista retornada =====");
        return "auditoria/cambios-operacionales";
    }

    /**
     * GET /web/auditoria/cambios-operacionales/descargar - Descargar auditoría de cambios operacionales en Excel
     */
    @GetMapping("/cambios-operacionales/descargar")
    @PreAuthorize(PermissionConstants.DIRECTORES)
    public ResponseEntity<byte[]> descargarAuditoriaCambiosOperacionalesExcel() {
        try {
            List<AuditoriaDTO> cambios = new ArrayList<>();
            
            AuditReader auditReader = AuditReaderFactory.get(entityManager);
            
            // Obtener auditoría de Laboratorios
            cambios.addAll(obtenerAuditoriaEntidad(auditReader, Laboratorio.class, "Laboratorio"));
            
            // Obtener auditoría de Equipos
            cambios.addAll(obtenerAuditoriaEntidad(auditReader, Equipo.class, "Equipo"));
            
            // Obtener auditoría de Novedades
            cambios.addAll(obtenerAuditoriaEntidad(auditReader, Novedad.class, "Novedad"));
            
            // Ordenar por fecha descendente
            Collections.sort(cambios, (a, b) -> {
                if (a.getFechaCambio() == null || b.getFechaCambio() == null) return 0;
                return b.getFechaCambio().compareTo(a.getFechaCambio());
            });
            
            // Generar Excel
            byte[] excelBytes = auditoriaService.exportarAExcel(cambios);
            
            // Preparar respuesta
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.valueOf("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.setContentDispositionFormData("attachment", "auditoria_cambios_operacionales_" + System.currentTimeMillis() + ".xlsx");
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(excelBytes);
                    
        } catch (Exception e) {
            logger.error("[Auditoria] Error inesperado", e);
            return ResponseEntity.internalServerError().build();
        }
    }

}


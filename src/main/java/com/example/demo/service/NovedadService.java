package com.example.demo.service;

import com.example.demo.dto.CambiarEstadoNovedadDTO;
import com.example.demo.dto.ImagenNovedadRequestDTO;
import com.example.demo.dto.ImagenNovedadResponseDTO;
import com.example.demo.dto.NovedadRequestDTO;
import com.example.demo.dto.NovedadResponseDTO;
import com.example.demo.entity.*;
import com.example.demo.enums.EstadoNovedad;
import com.example.demo.enums.TipoNovedad;
import com.example.demo.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;



@Service
@RequiredArgsConstructor
public class NovedadService {

    private static final Set<String> PRIORIDADES_VALIDAS = Set.of("BAJA", "MEDIA", "ALTA", "CRITICA");

    // Tipos de imagen permitidos para adjuntar a novedades
    private static final Set<String> MIME_TYPES_PERMITIDOS = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp"
    );

    // Tamaño maximo de imagen en Base64: ~4 MB en bytes originales = ~5.3 MB en Base64
    // Se limita el string Base64 a 6 MB de caracteres como maximo seguro
    private static final int MAX_IMAGEN_BASE64_CHARS = 6 * 1024 * 1024;

    private final NovedadRepository novedadRepository;
    private final ImagenNovedadRepository imagenNovedadRepository;
    private final EntityLookupService entityLookup;

    @Transactional
    public NovedadResponseDTO reportarNovedad(String username, NovedadRequestDTO requestDTO) {
        Usuario usuario = entityLookup.findUsuarioByUsernameOrThrow(username);

        if (requestDTO.getPrioridad() == null || !PRIORIDADES_VALIDAS.contains(requestDTO.getPrioridad())) {
            throw new RuntimeException("Prioridad inválida. Valores permitidos: BAJA, MEDIA, ALTA, CRITICA");
        }

        Laboratorio laboratorio = entityLookup.findLaboratorioOrThrow(requestDTO.getLaboratorioId());

        Equipo equipo = null;
        if (requestDTO.getTipo() == TipoNovedad.EQUIPO) {
            if (requestDTO.getEquipoId() == null) {
                throw new RuntimeException("El ID del equipo es obligatorio para novedades de tipo EQUIPO");
            }
            equipo = entityLookup.findEquipoOrThrow(requestDTO.getEquipoId());
            entityLookup.validateEquipoInLaboratorio(equipo, laboratorio);
        } else {
            requestDTO.setEquipoId(null);
        }

        Novedad novedad = new Novedad();
        novedad.setTitulo(requestDTO.getTitulo());
        novedad.setDescripcion(requestDTO.getDescripcion());
        novedad.setTipo(requestDTO.getTipo());
        novedad.setEstado(requestDTO.getEstado() != null ? requestDTO.getEstado() : EstadoNovedad.PENDIENTE);
        novedad.setPrioridad(requestDTO.getPrioridad());
        novedad.setUsuarioReporta(usuario);
        novedad.setLaboratorio(laboratorio);
        novedad.setEquipo(equipo);
        novedad.setFechaReporte(LocalDateTime.now());

        Novedad guardada = novedadRepository.save(novedad);
        return convertirAResponseDTO(guardada, 0);
    }

    @Transactional
    public NovedadResponseDTO cambiarEstado(Long novedadId, String username, CambiarEstadoNovedadDTO requestDTO) {
        Novedad novedad = entityLookup.findNovedadOrThrow(novedadId);

        novedad.setEstado(requestDTO.getNuevoEstado());

        if (requestDTO.getNuevoEstado() == EstadoNovedad.SOLUCIONADA) {
            novedad.setFechaResolucion(LocalDateTime.now());
            novedad.setObservacionesResolucion(requestDTO.getObservacionesResolucion());
        }

        Novedad actualizada = novedadRepository.save(novedad);
        int count = (int) imagenNovedadRepository.countByNovedadId(novedadId);
        return convertirAResponseDTO(actualizada, count);
    }

    @Transactional
    public ImagenNovedadResponseDTO adjuntarImagen(Long novedadId, String username,
            ImagenNovedadRequestDTO requestDTO) {
        Novedad novedad = entityLookup.findNovedadOrThrow(novedadId);

        // Validar que el tipo de imagen sea permitido
        String tipoMime = requestDTO.getTipoMime();
        if (tipoMime == null || !MIME_TYPES_PERMITIDOS.contains(tipoMime.toLowerCase())) {
            throw new RuntimeException(
                    "Tipo de imagen no permitido. Solo se aceptan: jpeg, png, gif, webp");
        }

        // Validar que el contenido Base64 no exceda el tamaño maximo
        String base64 = requestDTO.getImagenBase64();
        if (base64 == null || base64.isBlank()) {
            throw new RuntimeException("La imagen no puede estar vacia");
        }
        if (base64.length() > MAX_IMAGEN_BASE64_CHARS) {
            throw new RuntimeException("La imagen excede el tamaño maximo permitido (4 MB)");
        }

        ImagenNovedad imagen = new ImagenNovedad();
        imagen.setNovedad(novedad);
        imagen.setNombreArchivo(requestDTO.getNombreArchivo());
        imagen.setTipoMime(tipoMime.toLowerCase());
        imagen.setImagenBase64(base64);

        return convertirAImagenResponseDTO(imagenNovedadRepository.save(imagen), false);
    }

    @Transactional(readOnly = true)
    public List<NovedadResponseDTO> listarTodas() {
        List<Novedad> novedades = novedadRepository.findAllWithRelationsUnpaged();
        return mapNovedadesConConteoImagenes(novedades);
    }

    @Transactional(readOnly = true)
    public List<NovedadResponseDTO> listarPorEstado(EstadoNovedad estado) {
        List<Novedad> novedades = novedadRepository.findByEstadoWithRelationsUnpaged(estado);
        return mapNovedadesConConteoImagenes(novedades);
    }

    @Transactional(readOnly = true)
    public List<NovedadResponseDTO> listarMisNovedades(String username) {
        Usuario usuario = entityLookup.findUsuarioByUsernameOrThrow(username);
        List<Novedad> novedades = novedadRepository.findByUsuarioReportaIdWithRelations(usuario.getId());
        return mapNovedadesConConteoImagenes(novedades);
    }

    @Transactional(readOnly = true)
    public List<NovedadResponseDTO> listarPorCarrera(String carrera) {
        List<Novedad> novedades = novedadRepository.findByCarreraWithRelations(carrera);
        return mapNovedadesConConteoImagenes(novedades);
    }

    @Transactional(readOnly = true)
    public NovedadResponseDTO obtenerPorId(Long id) {
        Novedad novedad = entityLookup.findNovedadOrThrow(id);
        int count = (int) imagenNovedadRepository.countByNovedadId(id);
        return convertirAResponseDTO(novedad, count);
    }

    @Transactional(readOnly = true)
    public List<ImagenNovedadResponseDTO> obtenerImagenes(Long novedadId) {
        return imagenNovedadRepository.findByNovedadId(novedadId).stream()
                .map(imagen -> convertirAImagenResponseDTO(imagen, true))
                .collect(Collectors.toList());
    }

    private List<NovedadResponseDTO> mapNovedadesConConteoImagenes(List<Novedad> novedades) {
        if (novedades.isEmpty()) return Collections.emptyList();
        List<Long> ids = novedades.stream().map(Novedad::getId).collect(Collectors.toList());
        Map<Long, Integer> conteoImagenes = buildImageCountMap(ids);
        return novedades.stream()
                .map(n -> convertirAResponseDTO(n, conteoImagenes.getOrDefault(n.getId(), 0)))
                .collect(Collectors.toList());
    }

    private Map<Long, Integer> buildImageCountMap(List<Long> novedadIds) {
        return imagenNovedadRepository.countGroupByNovedadIdIn(novedadIds).stream()
                .collect(Collectors.toMap(
                        arr -> (Long) arr[0],
                        arr -> ((Long) arr[1]).intValue()
                ));
    }

    private NovedadResponseDTO convertirAResponseDTO(Novedad novedad, int cantidadImagenes) {
        NovedadResponseDTO dto = new NovedadResponseDTO();
        dto.setId(novedad.getId());
        dto.setTitulo(novedad.getTitulo());
        dto.setDescripcion(novedad.getDescripcion());
        dto.setTipo(novedad.getTipo());
        dto.setEstado(novedad.getEstado());
        dto.setPrioridad(novedad.getPrioridad());

        if (novedad.getUsuarioReporta() != null) {
            dto.setUsuarioReportaId(novedad.getUsuarioReporta().getId());
            dto.setUsuarioReportaNombre(novedad.getUsuarioReporta().getNombreCompleto());
        }

        if (novedad.getLaboratorio() != null) {
            dto.setLaboratorioId(novedad.getLaboratorio().getId());
            dto.setLaboratorioNombre(novedad.getLaboratorio().getNombre());
        }

        if (novedad.getEquipo() != null) {
            dto.setEquipoId(novedad.getEquipo().getId());
            dto.setEquipoCodigo(novedad.getEquipo().getCodigo());
        }

        dto.setFechaReporte(novedad.getFechaReporte());
        dto.setFechaResolucion(novedad.getFechaResolucion());
        dto.setObservacionesResolucion(novedad.getObservacionesResolucion());
        dto.setCantidadImagenes(cantidadImagenes);
        dto.setCreatedAt(novedad.getCreatedAt());
        dto.setUpdatedAt(novedad.getUpdatedAt());
        return dto;
    }

    private ImagenNovedadResponseDTO convertirAImagenResponseDTO(ImagenNovedad imagen, boolean incluirBase64) {
        ImagenNovedadResponseDTO dto = new ImagenNovedadResponseDTO();
        dto.setId(imagen.getId());
        dto.setNovedadId(imagen.getNovedad().getId());
        dto.setNombreArchivo(imagen.getNombreArchivo());
        dto.setTipoMime(imagen.getTipoMime());
        dto.setImagenBase64(incluirBase64 ? imagen.getImagenBase64() : null);
        dto.setCreatedAt(imagen.getCreatedAt());
        return dto;
    }
}

package com.example.demo.service;

import com.example.demo.dto.EquipoRequestDTO;
import com.example.demo.dto.EquipoResponseDTO;
import com.example.demo.entity.Equipo;
import com.example.demo.repository.EquipoRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

//Servicio para gestión de Equipos
@Service
@RequiredArgsConstructor
public class EquipoService {

    private final EquipoRepository equipoRepository;
    private final EntityLookupService entityLookup;
    private final MapperService mapperService;
    private final EntityManager entityManager;
    
    private static final Logger logger = LoggerFactory.getLogger(EquipoService.class);

    @Transactional(readOnly = true)
    public List<EquipoResponseDTO> listarTodos() {
        return mapperService.mapList(
                equipoRepository.findAllActive(),
                this::convertirAResponseDTO
        );
    }

    @Transactional(readOnly = true)
    public List<EquipoResponseDTO> listarPorLaboratorio(Long laboratorioId) {
        return mapperService.mapList(
                equipoRepository.findByLaboratorioIdAndNotDeleted(laboratorioId),
                this::convertirAResponseDTO
        );
    }

    @Transactional(readOnly = true)
    public List<EquipoResponseDTO> listarPorEstado(String estado) {
        return mapperService.mapList(
                equipoRepository.findByEstadoAndNotDeleted(estado),
                this::convertirAResponseDTO
        );
    }

    @Transactional(readOnly = true)
    public EquipoResponseDTO obtenerPorId(Long id) {
        Equipo equipo = entityLookup.findEquipoOrThrow(id);
        return convertirAResponseDTO(equipo);
    }

    @Transactional
    public EquipoResponseDTO crear(EquipoRequestDTO requestDTO) {
        // Validar que no exista otro equipo con el mismo código
        if (equipoRepository.findByCodigo(requestDTO.getCodigo()).isPresent()) {
            throw new RuntimeException("Ya existe un equipo con el código: " + requestDTO.getCodigo());
        }

        // Validar que el laboratorio existe
        var laboratorio = entityLookup.findLaboratorioOrThrow(requestDTO.getLaboratorioId());

        Equipo equipo = new Equipo();
        equipo.setCodigo(requestDTO.getCodigo());
        equipo.setTipo(requestDTO.getTipo());
        equipo.setMarca(requestDTO.getMarca());
        equipo.setModelo(requestDTO.getModelo());
        equipo.setNumeroSerie(requestDTO.getNumeroSerie());
        equipo.setEstado(requestDTO.getEstado());
        equipo.setLaboratorio(laboratorio);

        logger.info("[Equipo] Creando nuevo equipo: código={}", requestDTO.getCodigo());
        
        Equipo guardado = equipoRepository.save(equipo);
        entityManager.flush(); // Forzar a Hibernate a procesar cambios ahora (para Envers)
        
        logger.info("[Equipo] Equipo creado exitosamente: id={}, código={}", guardado.getId(), guardado.getCodigo());
        return convertirAResponseDTO(guardado);
    }

    @Transactional
    public EquipoResponseDTO actualizar(Long id, EquipoRequestDTO requestDTO) {
        Equipo equipo = entityLookup.findEquipoOrThrow(id);

        // Validar código único (excepto el mismo equipo)
        equipoRepository.findByCodigo(requestDTO.getCodigo())
                .ifPresent(eq -> {
                    if (!eq.getId().equals(id)) {
                        throw new RuntimeException("Ya existe otro equipo con el código: " + requestDTO.getCodigo());
                    }
                });

        // Validar laboratorio
        var laboratorio = entityLookup.findLaboratorioOrThrow(requestDTO.getLaboratorioId());

        logger.info("[Equipo] Actualizando equipo: id={}, código={}", id, requestDTO.getCodigo());
        
        equipo.setCodigo(requestDTO.getCodigo());
        equipo.setTipo(requestDTO.getTipo());
        equipo.setMarca(requestDTO.getMarca());
        equipo.setModelo(requestDTO.getModelo());
        equipo.setNumeroSerie(requestDTO.getNumeroSerie());
        equipo.setEstado(requestDTO.getEstado());
        equipo.setLaboratorio(laboratorio);

        Equipo actualizado = equipoRepository.save(equipo);
        entityManager.flush(); // Forzar a Hibernate a procesar cambios ahora (para Envers)
        
        logger.info("[Equipo] Equipo actualizado exitosamente: id={}", actualizado.getId());
        return convertirAResponseDTO(actualizado);
    }

    @Transactional
    public void eliminar(Long id) {
        Equipo equipo = entityLookup.findEquipoOrThrow(id);
        equipoRepository.delete(equipo);
    }

    // Soft delete: marca el equipo como eliminado sin borrarlo físicamente
    @Transactional
    public void softDelete(Long id) {
        Equipo equipo = entityLookup.findEquipoOrThrow(id);

        logger.info("[Equipo] Soft-eliminando equipo: id={}, código={}", id, equipo.getCodigo());
        
        equipo.softDelete();
        equipoRepository.save(equipo);
        entityManager.flush(); // Forzar a Hibernate a procesar cambios ahora (para Envers)
        
        logger.info("[Equipo] Equipo soft-eliminado exitosamente: id={}", id);
    }

    // Restaura un equipo eliminado (soft delete)
    @Transactional
    public EquipoResponseDTO restore(Long id) {
        Equipo equipo = entityLookup.findEquipoOrThrow(id);

        if (!equipo.isDeleted()) {
            throw new RuntimeException("El equipo no está eliminado, no se puede restaurar");
        }

        logger.info("[Equipo] Restaurando equipo: id={}, código={}", id, equipo.getCodigo());
        
        equipo.restore();
        Equipo restaurado = equipoRepository.save(equipo);
        entityManager.flush(); // Forzar a Hibernate a procesar cambios ahora (para Envers)
        
        logger.info("[Equipo] Equipo restaurado exitosamente: id={}", restaurado.getId());
        return convertirAResponseDTO(restaurado);
    }

    // Obtiene todos los equipos activos (no eliminados)
    @Transactional(readOnly = true)
    public List<EquipoResponseDTO> listarActivos() {
        return mapperService.mapList(
                equipoRepository.findAllActive(),
                this::convertirAResponseDTO
        );
    }

    // Obtiene todos los equipos eliminados (papelera)
    @Transactional(readOnly = true)
    public List<EquipoResponseDTO> listarEliminados() {
        return mapperService.mapList(
                equipoRepository.findAllDeleted(),
                this::convertirAResponseDTO
        );
    }

    @Transactional
    public void hardDelete(Long id) {
        Equipo equipo = entityLookup.findEquipoOrThrow(id);
        equipoRepository.delete(equipo);
    }

    private EquipoResponseDTO convertirAResponseDTO(Equipo equipo) {
        EquipoResponseDTO dto = new EquipoResponseDTO();
        dto.setId(equipo.getId());
        dto.setCodigo(equipo.getCodigo());
        dto.setTipo(equipo.getTipo());
        dto.setMarca(equipo.getMarca());
        dto.setModelo(equipo.getModelo());
        dto.setNumeroSerie(equipo.getNumeroSerie());
        dto.setEstado(equipo.getEstado());
        dto.setLaboratorioId(equipo.getLaboratorio().getId());
        dto.setLaboratorioNombre(equipo.getLaboratorio().getNombre());
        dto.setCreatedAt(equipo.getCreatedAt());
        dto.setUpdatedAt(equipo.getUpdatedAt());
        return dto;
    }
}

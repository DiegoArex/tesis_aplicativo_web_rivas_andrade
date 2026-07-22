package com.example.demo.service;

import com.example.demo.dto.LaboratorioRequestDTO;
import com.example.demo.dto.LaboratorioResponseDTO;
import com.example.demo.entity.Laboratorio;
import com.example.demo.repository.LaboratorioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

//Servicio para gestión de Laboratorios
@Service
@RequiredArgsConstructor
public class LaboratorioService {
    
    private final LaboratorioRepository laboratorioRepository;
    private final EntityLookupService entityLookup;
    private final MapperService mapperService;

    @Transactional(readOnly = true)
    public List<LaboratorioResponseDTO> listarTodos() {
        // Se usa findAllActive() para no mostrar laboratorios eliminados (soft-delete)
        // a usuarios autenticados que consulten GET /api/laboratorios
        return mapperService.mapList(
                laboratorioRepository.findAllActive(),
                this::convertirAResponseDTO
        );
    }

    @Transactional(readOnly = true)
    public List<LaboratorioResponseDTO> listarActivos() {
        return mapperService.mapList(
                laboratorioRepository.findByActivoTrue(),
                this::convertirAResponseDTO
        );
    }

    @Transactional(readOnly = true)
    public List<LaboratorioResponseDTO> listarDisponiblesParaEntrada() {
        // Solo laboratorios activos y no eliminados que no estén en uso
        List<Laboratorio> activos = laboratorioRepository.findByActivoTrueAndNotDeleted();
        return mapperService.mapList(
                activos,
                this::convertirAResponseDTO
        );
    }

    @Transactional(readOnly = true)
    public LaboratorioResponseDTO obtenerPorId(Long id) {
        Laboratorio laboratorio = entityLookup.findLaboratorioOrThrow(id);
        return convertirAResponseDTO(laboratorio);
    }

    @Transactional
    public LaboratorioResponseDTO crear(LaboratorioRequestDTO requestDTO) {
        // Validar que no exista otro laboratorio con el mismo nombre
        if (laboratorioRepository.findByNombre(requestDTO.getNombre()).isPresent()) {
            throw new RuntimeException("Ya existe un laboratorio con el nombre: " + requestDTO.getNombre());
        }

        Laboratorio laboratorio = new Laboratorio();
        laboratorio.setNombre(requestDTO.getNombre());
        laboratorio.setUbicacion(requestDTO.getUbicacion());
        laboratorio.setCapacidad(requestDTO.getCapacidad());
        laboratorio.setDescripcion(requestDTO.getDescripcion());
        laboratorio.setActivo(requestDTO.getActivo() != null ? requestDTO.getActivo() : true);

        Laboratorio guardado = laboratorioRepository.save(laboratorio);
        return convertirAResponseDTO(guardado);
    }

    @Transactional
    public LaboratorioResponseDTO actualizar(Long id, LaboratorioRequestDTO requestDTO) {
        Laboratorio laboratorio = entityLookup.findLaboratorioOrThrow(id);

        // Validar nombre único (excepto el mismo laboratorio)
        laboratorioRepository.findByNombre(requestDTO.getNombre())
                .ifPresent(lab -> {
                    if (!lab.getId().equals(id)) {
                        throw new RuntimeException(
                                "Ya existe otro laboratorio con el nombre: " + requestDTO.getNombre());
                    }
                });

        laboratorio.setNombre(requestDTO.getNombre());
        laboratorio.setUbicacion(requestDTO.getUbicacion());
        laboratorio.setCapacidad(requestDTO.getCapacidad());
        laboratorio.setDescripcion(requestDTO.getDescripcion());
        laboratorio.setActivo(requestDTO.getActivo());

        Laboratorio actualizado = laboratorioRepository.save(laboratorio);
        return convertirAResponseDTO(actualizado);
    }

    @Transactional
    public void eliminar(Long id) {
        // Se usa softDelete() que establece tanto activo=false como deletedAt=ahora,
        // manteniendo consistencia con isDeleted() y los metodos de busqueda del repositorio
        softDelete(id);
    }

    // Soft delete: marca el laboratorio como eliminado sin borrarlo físicamente
    @Transactional
    public void softDelete(Long id) {
        Laboratorio laboratorio = entityLookup.findLaboratorioOrThrow(id);

        laboratorio.softDelete();
        laboratorioRepository.save(laboratorio);
    }

    // Restaura un laboratorio eliminado (soft delete)
    @Transactional
    public LaboratorioResponseDTO restore(Long id) {
        Laboratorio laboratorio = entityLookup.findLaboratorioOrThrow(id);

        if (!laboratorio.isDeleted()) {
            throw new RuntimeException("El laboratorio no está eliminado, no se puede restaurar");
        }

        laboratorio.restore();
        Laboratorio restaurado = laboratorioRepository.save(laboratorio);
        return convertirAResponseDTO(restaurado);
    }

    // Obtiene todos los laboratorios activos (no eliminados)
    @Transactional(readOnly = true)
    public List<LaboratorioResponseDTO> listarActivosNoEliminados() {
        return mapperService.mapList(
                laboratorioRepository.findAllActive(),
                this::convertirAResponseDTO
        );
    }

    // Obtiene todos los laboratorios eliminados (papelera)
    @Transactional(readOnly = true)
    public List<LaboratorioResponseDTO> listarEliminados() {
        return mapperService.mapList(
                laboratorioRepository.findAllDeleted(),
                this::convertirAResponseDTO
        );
    }

    // Hard delete: elimina permanentemente el laboratorio de la base de datos
    // PRECAUCIÓN: Esta operación NO se puede deshacer
    @Transactional
    public void hardDelete(Long id) {
        Laboratorio laboratorio = entityLookup.findLaboratorioOrThrow(id);

        laboratorioRepository.delete(laboratorio);
    }

    private LaboratorioResponseDTO convertirAResponseDTO(Laboratorio laboratorio) {
        LaboratorioResponseDTO dto = new LaboratorioResponseDTO();
        dto.setId(laboratorio.getId());
        dto.setNombre(laboratorio.getNombre());
        dto.setUbicacion(laboratorio.getUbicacion());
        dto.setCapacidad(laboratorio.getCapacidad());
        dto.setDescripcion(laboratorio.getDescripcion());
        dto.setActivo(laboratorio.getActivo());
        dto.setCantidadEquipos(laboratorio.getEquipos() != null ? laboratorio.getEquipos().size() : 0);
        dto.setCreatedAt(laboratorio.getCreatedAt());
        dto.setUpdatedAt(laboratorio.getUpdatedAt());
        return dto;
    }
}

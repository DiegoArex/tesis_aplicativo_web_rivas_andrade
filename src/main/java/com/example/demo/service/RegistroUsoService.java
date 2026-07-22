package com.example.demo.service;

import com.example.demo.dto.RegistroUsoEntradaDTO;
import com.example.demo.dto.RegistroUsoResponseDTO;
import com.example.demo.dto.RegistroUsoSalidaDTO;
import com.example.demo.entity.Laboratorio;
import com.example.demo.entity.RegistroUso;
import com.example.demo.entity.Usuario;
import com.example.demo.enums.TipoRegistro;
import com.example.demo.repository.RegistroUsoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

//Servicio para gestión de Registros de Uso
@Service
@RequiredArgsConstructor
public class RegistroUsoService {

    private final RegistroUsoRepository registroUsoRepository;
    private final EntityLookupService entityLookup;
    private final MapperService mapperService;
    private final UsuarioService usuarioService;

    @Transactional
    public RegistroUsoResponseDTO registrarEntrada(String username, String keycloakId, String email, String nombreCompleto, String roles,
            RegistroUsoEntradaDTO requestDTO) {
        // Obtener o crear usuario automáticamente desde Keycloak
        Usuario usuario = usuarioService.obtenerOCrearUsuarioDesdeJWT(username, keycloakId, email, nombreCompleto, roles);

        // Verificar que no tenga un registro activo
        registroUsoRepository.findByUsuarioIdAndFechaSalidaIsNull(usuario.getId())
                .ifPresent(registro -> {
                    String laboratorios = registro.getLaboratorio().getNombre();
                    if (registro.getLaboratorioSecundario() != null) {
                        laboratorios += " y " + registro.getLaboratorioSecundario().getNombre();
                    }
                    throw new RuntimeException("El usuario ya tiene un registro activo en: " + laboratorios);
                });

        // Validar laboratorio principal
        Laboratorio laboratorio = entityLookup.findLaboratorioOrThrow(requestDTO.getLaboratorioId());

        // Crear registro
        RegistroUso registro = new RegistroUso();
        registro.setUsuario(usuario);
        registro.setLaboratorio(laboratorio);
        registro.setFechaEntrada(LocalDateTime.now());
        registro.setProposito(requestDTO.getProposito());
        registro.setObservaciones(requestDTO.getObservaciones());

        // Manejar examen con laboratorio secundario
        if (requestDTO.getEsExamen() != null && requestDTO.getEsExamen() && 
            requestDTO.getLaboratorioSecundarioId() != null) {
            
            // Validar laboratorio secundario
            Laboratorio laboratorioSecundario = entityLookup.findLaboratorioOrThrow(requestDTO.getLaboratorioSecundarioId());
            
            // Verificar que no sea el mismo laboratorio
            entityLookup.validateLabsAreDifferent(laboratorio, laboratorioSecundario);
            
            registro.setLaboratorioSecundario(laboratorioSecundario);
            registro.setTipoRegistro(TipoRegistro.EXAMEN);
        } else {
            registro.setTipoRegistro(TipoRegistro.NORMAL);
        }

        RegistroUso guardado = registroUsoRepository.save(registro);
        return convertirAResponseDTO(guardado);
    }

    @Transactional
    public RegistroUsoResponseDTO registrarSalida(String username, RegistroUsoSalidaDTO requestDTO) {
        // Obtener usuario
        Usuario usuario = entityLookup.findUsuarioByUsernameOrThrow(username);

        // Buscar registro activo
        RegistroUso registro = entityLookup.findActiveRegistroOrThrow(usuario.getId());

        registro.setFechaSalida(LocalDateTime.now());
        if (requestDTO.getObservaciones() != null && !requestDTO.getObservaciones().isEmpty()) {
            String obsActuales = registro.getObservaciones() != null ? registro.getObservaciones() + " | " : "";
            registro.setObservaciones(obsActuales + requestDTO.getObservaciones());
        }

        RegistroUso actualizado = registroUsoRepository.save(registro);
        return convertirAResponseDTO(actualizado);
    }

    @Transactional(readOnly = true)
    public List<RegistroUsoResponseDTO> obtenerTodosRegistros() {
        return mapperService.mapList(
                registroUsoRepository.findAll(),
                this::convertirAResponseDTO
        );
    }

    @Transactional(readOnly = true)
    public List<RegistroUsoResponseDTO> obtenerMisRegistros(String username) {
        Usuario usuario = entityLookup.findUsuarioByUsernameOrThrow(username);
        return mapperService.mapList(
                registroUsoRepository.findByUsuarioId(usuario.getId()),
                this::convertirAResponseDTO
        );
    }

    @Transactional(readOnly = true)
    public List<RegistroUsoResponseDTO> obtenerRegistrosPorUsuario(Long usuarioId) {
        return mapperService.mapList(
                registroUsoRepository.findByUsuarioId(usuarioId),
                this::convertirAResponseDTO
        );
    }

    @Transactional(readOnly = true)
    public List<RegistroUsoResponseDTO> obtenerRegistrosPorLaboratorio(Long laboratorioId) {
        return mapperService.mapList(
                registroUsoRepository.findByLaboratorioId(laboratorioId),
                this::convertirAResponseDTO
        );
    }

    @Transactional(readOnly = true)
    public List<RegistroUsoResponseDTO> obtenerRegistrosActivos() {
        return mapperService.mapList(
                registroUsoRepository.findByFechaSalidaIsNull(),
                this::convertirAResponseDTO
        );
    }

    @Transactional(readOnly = true)
    public List<RegistroUsoResponseDTO> obtenerRegistrosPorRangoFechas(LocalDateTime inicio, LocalDateTime fin) {
        return mapperService.mapList(
                registroUsoRepository.findByFechaEntradaBetween(inicio, fin),
                this::convertirAResponseDTO
        );
    }

    private RegistroUsoResponseDTO convertirAResponseDTO(RegistroUso registro) {
        RegistroUsoResponseDTO dto = new RegistroUsoResponseDTO();
        dto.setId(registro.getId());
        dto.setUsuarioId(registro.getUsuario().getId());
        dto.setUsuarioNombre(registro.getUsuario().getNombreCompleto());
        dto.setLaboratorioId(registro.getLaboratorio().getId());
        dto.setLaboratorioNombre(registro.getLaboratorio().getNombre());
        
        // Agregar información del laboratorio secundario si existe
        if (registro.getLaboratorioSecundario() != null) {
            dto.setLaboratorioNombre(registro.getLaboratorio().getNombre() + " y " + 
                                     registro.getLaboratorioSecundario().getNombre());
        }
        
        dto.setFechaEntrada(registro.getFechaEntrada());
        dto.setFechaSalida(registro.getFechaSalida());
        dto.setProposito(registro.getProposito());
        dto.setObservaciones(registro.getObservaciones());
        dto.setActivo(registro.getFechaSalida() == null);
        
        // Asignar tipo de registro (por defecto NORMAL si es null)
        if (registro.getTipoRegistro() != null) {
            dto.setTipoRegistro(registro.getTipoRegistro().getNombre());
        } else {
            dto.setTipoRegistro(TipoRegistro.NORMAL.getNombre());
        }

        // Calcular duración si hay salida
        if (registro.getFechaSalida() != null) {
            Duration duracion = Duration.between(registro.getFechaEntrada(), registro.getFechaSalida());
            dto.setDuracionMinutos(duracion.toMinutes());
        }

        dto.setCreatedAt(registro.getCreatedAt());
        return dto;
    }
}

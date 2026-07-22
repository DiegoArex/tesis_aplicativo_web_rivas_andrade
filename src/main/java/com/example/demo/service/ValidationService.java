package com.example.demo.service;

import com.example.demo.exception.EntityNotFoundException;
import com.example.demo.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Servicio centralizado para validaciones comunes.
 * 
 * Consolida lógica repetida de validaciones:
 *   if (repository.findByXXX(...).isPresent()) {
 *       throw new RuntimeException("Ya existe...");
 *   }
 * 
 * Proporciona métodos específicos para cada tipo de validación.
 * 
 * Beneficios:
 * - ✅ Código DRY (Don't Repeat Yourself)
 * - ✅ Validaciones consistentes
 * - ✅ Mensajes de error uniformes
 * - ✅ Fácil de mantener y actualizar
 * 
 * Ejemplos de uso:
 * 
 * // Validar nombre único
 * validationService.validarLaboratorioNombreUnico(nombre, laboratorioId);
 * 
 * // Validar email único
 * validationService.validarUsuarioEmailUnico(email, usuarioId);
 */
@Service
@RequiredArgsConstructor
public class ValidationService {
    
    private final LaboratorioRepository laboratorioRepository;
    private final EquipoRepository equipoRepository;
    private final UsuarioRepository usuarioRepository;
    private final NovedadRepository novedadRepository;
    
    // ======================== LABORATORIO ========================
    
    /**
     * Valida que no exista otro laboratorio con el mismo nombre.
     * 
     * @param nombre Nombre del laboratorio a validar
     * @param excludeId ID del laboratorio a excluir de la búsqueda (para actualizaciones)
     * @throws RuntimeException si ya existe otro laboratorio con ese nombre
     */
    public void validarLaboratorioNombreUnico(String nombre, Long excludeId) {
        laboratorioRepository.findByNombre(nombre)
                .ifPresent(lab -> {
                    if (excludeId == null || !lab.getId().equals(excludeId)) {
                        throw new RuntimeException(
                                "Ya existe un laboratorio con el nombre: " + nombre);
                    }
                });
    }
    
    /**
     * Valida que un laboratorio exista.
     * 
     * @param laboratorioId ID del laboratorio
     * @throws EntityNotFoundException si no existe
     */
    public void validarLaboratorioExiste(Long laboratorioId) {
        if (!laboratorioRepository.existsById(laboratorioId)) {
            throw new EntityNotFoundException("Laboratorio", laboratorioId);
        }
    }
    
    // ======================== EQUIPO ========================
    
    /**
     * Valida que no exista otro equipo con el mismo código.
     * 
     * @param codigo Código del equipo a validar
     * @param excludeId ID del equipo a excluir de la búsqueda (para actualizaciones)
     * @throws RuntimeException si ya existe otro equipo con ese código
     */
    public void validarEquipoCodigoUnico(String codigo, Long excludeId) {
        equipoRepository.findByCodigo(codigo)
                .ifPresent(eq -> {
                    if (excludeId == null || !eq.getId().equals(excludeId)) {
                        throw new RuntimeException(
                                "Ya existe un equipo con el código: " + codigo);
                    }
                });
    }
    
    /**
     * Valida que un equipo exista.
     * 
     * @param equipoId ID del equipo
     * @throws EntityNotFoundException si no existe
     */
    public void validarEquipoExiste(Long equipoId) {
        if (!equipoRepository.existsById(equipoId)) {
            throw new EntityNotFoundException("Equipo", equipoId);
        }
    }
    
    // ======================== USUARIO ========================
    
    /**
     * Valida que no exista otro usuario con el mismo username.
     * 
     * @param username Username del usuario a validar
     * @param excludeId ID del usuario a excluir de la búsqueda (para actualizaciones)
     * @throws RuntimeException si ya existe otro usuario con ese username
     */
    public void validarUsuarioUsernameUnico(String username, Long excludeId) {
        usuarioRepository.findByUsername(username)
                .ifPresent(u -> {
                    if (excludeId == null || !u.getId().equals(excludeId)) {
                        throw new RuntimeException(
                                "Ya existe un usuario con el username: " + username);
                    }
                });
    }
    
    /**
     * Valida que no exista otro usuario con el mismo email.
     * 
     * @param email Email del usuario a validar
     * @param excludeId ID del usuario a excluir de la búsqueda (para actualizaciones)
     * @throws RuntimeException si ya existe otro usuario con ese email
     */
    public void validarUsuarioEmailUnico(String email, Long excludeId) {
        usuarioRepository.findByEmail(email)
                .ifPresent(u -> {
                    if (excludeId == null || !u.getId().equals(excludeId)) {
                        throw new RuntimeException(
                                "Ya existe un usuario con el email: " + email);
                    }
                });
    }
    
    /**
     * Valida que un usuario exista.
     * 
     * @param usuarioId ID del usuario
     * @throws EntityNotFoundException si no existe
     */
    public void validarUsuarioExiste(Long usuarioId) {
        if (!usuarioRepository.existsById(usuarioId)) {
            throw new EntityNotFoundException("Usuario", usuarioId);
        }
    }
    
    // ======================== NOVEDAD ========================
    
    /**
     * Valida que una novedad exista.
     * 
     * @param novedadId ID de la novedad
     * @throws EntityNotFoundException si no existe
     */
    public void validarNovedadExiste(Long novedadId) {
        if (!novedadRepository.existsById(novedadId)) {
            throw new EntityNotFoundException("Novedad", novedadId);
        }
    }
    
    // ======================== GENÉRICOS ========================
    
    /**
     * Valida que un valor no esté vacío o null.
     * 
     * @param valor Valor a validar
     * @param nombreCampo Nombre del campo para el mensaje de error
     * @throws IllegalArgumentException si el valor está vacío o null
     */
    public void validarNoVacio(String valor, String nombreCampo) {
        if (valor == null || valor.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "El campo '" + nombreCampo + "' no puede estar vacío");
        }
    }
    
    /**
     * Valida que un número sea positivo.
     * 
     * @param valor Valor a validar
     * @param nombreCampo Nombre del campo para el mensaje de error
     * @throws IllegalArgumentException si el valor no es positivo
     */
    public void validarPositivo(Number valor, String nombreCampo) {
        if (valor == null || valor.doubleValue() <= 0) {
            throw new IllegalArgumentException(
                    "El campo '" + nombreCampo + "' debe ser un número positivo");
        }
    }
    
    /**
     * Valida que un email tenga formato válido.
     * 
     * @param email Email a validar
     * @throws IllegalArgumentException si el email no es válido
     */
    public void validarEmailFormato(String email) {
        if (email == null || !email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new IllegalArgumentException(
                    "El formato del email no es válido: " + email);
        }
    }
}

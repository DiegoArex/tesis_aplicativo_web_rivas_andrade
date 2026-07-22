package com.example.demo.service;

import com.example.demo.entity.*;
import com.example.demo.exception.EntityNotFoundException;
import com.example.demo.repository.EquipoRepository;
import com.example.demo.repository.ImagenNovedadRepository;
import com.example.demo.repository.LaboratorioRepository;
import com.example.demo.repository.NovedadRepository;
import com.example.demo.repository.RegistroUsoRepository;
import com.example.demo.repository.UsuarioRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio centralizado para búsquedas de entidades por ID.
 * 
 * Consolida la lógica repetida de:
 *   repository.findById(id).orElseThrow(...)
 * 
 * Proporciona métodos específicos para cada entidad con manejo consistente de excepciones.
 * 
 * Beneficios:
 * - ✅ Código DRY (Don't Repeat Yourself)
 * - ✅ Manejo de excepciones consistente
 * - ✅ Fácil de mantener y actualizar
 * - ✅ Mensajes de error uniformes
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EntityLookupService {
    
    private final UsuarioRepository usuarioRepository;
    private final LaboratorioRepository laboratorioRepository;
    private final EquipoRepository equipoRepository;
    private final NovedadRepository novedadRepository;
    private final RegistroUsoRepository registroUsoRepository;
    private final ImagenNovedadRepository imagenNovedadRepository;
    
    // ======================== USUARIO ========================
    
    /**
     * Busca un Usuario por ID.
     * 
     * @param id ID del usuario
     * @return Usuario encontrado
     * @throws EntityNotFoundException si no existe
     */
    public Usuario findUsuarioOrThrow(@NonNull Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuario", id));
    }
    
    /**
     * Busca un Usuario por username con fallback a email y keycloakId.
     * Si el usuario viene de Keycloak y no existe en BD, lo crea automáticamente.
     * 
     * Usa REQUIRES_NEW para crear una nueva transacción de escritura, permitiendo
     * crear usuarios automáticamente incluso cuando se llama desde métodos readOnly.
     * 
     * @param username Username, email o keycloakId del usuario
     * @return Usuario encontrado o creado
     * @throws EntityNotFoundException si no existe y no se puede crear
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Usuario findUsuarioByUsernameOrThrow(String username) {
        return usuarioRepository.findByUsername(username)
                .or(() -> usuarioRepository.findByEmail(username))
                .or(() -> usuarioRepository.findByKeycloakId(username))
                .orElseGet(() -> crearUsuarioKeycloakAutomatico(username));
    }
    
    /**
     * Crea un usuario automáticamente con datos mínimos del username de Keycloak.
     * Usado para sincronización automática de usuarios Keycloak.
     * 
     * @param username Username del usuario en Keycloak
     * @return Usuario creado con datos mínimos
     */
    private Usuario crearUsuarioKeycloakAutomatico(String username) {
        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setUsername(username);
        nuevoUsuario.setEmail(username + "@keycloak"); // Email temporal
        nuevoUsuario.setNombreCompleto(username); // Usar username como nombre temporal
        nuevoUsuario.setActivo(true);
        nuevoUsuario.setKeycloakId(username); // Considerar username como ID de Keycloak
        
        return usuarioRepository.save(nuevoUsuario);
    }
    
    // ======================== LABORATORIO ========================
    
    /**
     * Busca un Laboratorio por ID.
     * 
     * @param id ID del laboratorio
     * @return Laboratorio encontrado
     * @throws EntityNotFoundException si no existe
     */
    public Laboratorio findLaboratorioOrThrow(@NonNull Long id) {
        return laboratorioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Laboratorio", id));
    }
    
    /**
     * Busca un Laboratorio por nombre (código).
     * 
     * @param nombre Nombre del laboratorio
     * @return Laboratorio encontrado
     * @throws EntityNotFoundException si no existe
     */
    public Laboratorio findLaboratorioByNombreOrThrow(String nombre) {
        return laboratorioRepository.findByNombre(nombre)
                .orElseThrow(() -> new EntityNotFoundException("Laboratorio", nombre));
    }
    
    // ======================== EQUIPO ========================
    
    /**
     * Busca un Equipo por ID.
     * 
     * @param id ID del equipo
     * @return Equipo encontrado
     * @throws EntityNotFoundException si no existe
     */
    public Equipo findEquipoOrThrow(@NonNull Long id) {
        return equipoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Equipo", id));
    }
    
    /**
     * Busca un Equipo por código.
     * 
     * @param codigo Código del equipo
     * @return Equipo encontrado
     * @throws EntityNotFoundException si no existe
     */
    public Equipo findEquipoByCodigoOrThrow(String codigo) {
        return equipoRepository.findByCodigo(codigo)
                .orElseThrow(() -> new EntityNotFoundException("Equipo", codigo));
    }
    
    // ======================== NOVEDAD ========================
    
    /**
     * Busca una Novedad por ID.
     * 
     * @param id ID de la novedad
     * @return Novedad encontrada
     * @throws EntityNotFoundException si no existe
     */
    public Novedad findNovedadOrThrow(@NonNull Long id) {
        return novedadRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Novedad", id));
    }
    
    // ======================== REGISTRO DE USO ========================
    
    /**
     * Busca un Registro de Uso por ID.
     * 
     * @param id ID del registro
     * @return RegistroUso encontrado
     * @throws EntityNotFoundException si no existe
     */
    public RegistroUso findRegistroUsoOrThrow(@NonNull Long id) {
        return registroUsoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("RegistroUso", id));
    }
    
    /**
     * Busca un Registro activo (sin fecha de salida) para un usuario.
     * 
     * @param usuarioId ID del usuario
     * @return RegistroUso activo
     * @throws EntityNotFoundException si no existe
     */
    public RegistroUso findActiveRegistroOrThrow(@NonNull Long usuarioId) {
        return registroUsoRepository.findByUsuarioIdAndFechaSalidaIsNull(usuarioId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "RegistroUso activo para usuario", usuarioId));
    }
    
    // ======================== IMAGEN NOVEDAD ========================
    
    /**
     * Busca una Imagen de Novedad por ID.
     * 
     * @param id ID de la imagen
     * @return ImagenNovedad encontrada
     * @throws EntityNotFoundException si no existe
     */
    public ImagenNovedad findImagenNovedadOrThrow(@NonNull Long id) {
        return imagenNovedadRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("ImagenNovedad", id));
    }
    
    // ======================== VALIDACIONES COMUNES ========================
    
    /**
     * Valida que un equipo pertenezca a un laboratorio específico.
     * Lanza excepción si no es el caso.
     * 
     * @param equipo Equipo a validar
     * @param laboratorio Laboratorio donde debe estar el equipo
     * @throws IllegalArgumentException si el equipo no pertenece al laboratorio
     */
    public void validateEquipoInLaboratorio(Equipo equipo, Laboratorio laboratorio) {
        if (equipo.getLaboratorio() == null || 
            !equipo.getLaboratorio().getId().equals(laboratorio.getId())) {
            throw new IllegalArgumentException(
                    String.format("El equipo '%s' no pertenece al laboratorio '%s'",
                            equipo.getCodigo(), laboratorio.getNombre()));
        }
    }
    
    /**
     * Valida que dos laboratorios sean diferentes.
     * Utilizado en registros de examen con laboratorios primario y secundario.
     * 
     * @param lab1 Primer laboratorio
     * @param lab2 Segundo laboratorio
     * @throws IllegalArgumentException si son el mismo laboratorio
     */
    public void validateLabsAreDifferent(Laboratorio lab1, Laboratorio lab2) {
        if (lab1.getId().equals(lab2.getId())) {
            throw new IllegalArgumentException(
                    "Los laboratorios para examen deben ser diferentes");
        }
    }
}

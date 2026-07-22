package com.example.demo.security;

/**
 * Constantes centralizadas para permisos de la aplicación
 * Se utilizan en @PreAuthorize a nivel de controller
 * Los roles provienen de Keycloak con formato ROLE_* y son procesados por SecurityConfig
 */
public class PermissionConstants {

    // ===== EXPRESIONES DE AUTORIZACIÓN =====
    
    // Solo Administrador
    public static final String ADMIN_ONLY = "hasRole('ROLE_ADMIN')";

    // Solo autenticados
    public static final String AUTHENTICATED = "isAuthenticated()";

    // Docentes (Profesor, Director, Admin)
    public static final String DOCENTES = "hasAnyRole('ROLE_PROFESOR', 'ROLE_FACULTAD_DIRECTOR_CARRERA', 'ROLE_ADMIN')";

    // Directores (Director, Admin)
    public static final String DIRECTORES = "hasAnyRole('ROLE_FACULTAD_DIRECTOR_CARRERA', 'ROLE_ADMIN')";

    // Técnico, Docentes y Admin
    public static final String TECNICO_Y_DOCENTES = "hasAnyRole('ROLE_USUARIO', 'ROLE_PROFESOR', 'ROLE_FACULTAD_DIRECTOR_CARRERA', 'ROLE_ADMIN')";

    // Usuarios Registrados (Técnico, Docentes y Admin)
    public static final String USUARIOS_REGISTRADOS = "hasAnyRole('ROLE_USUARIO', 'ROLE_PROFESOR', 'ROLE_FACULTAD_DIRECTOR_CARRERA', 'ROLE_ADMIN')";

    // ===== REFERENCIAS POR ENDPOINT =====

    // Laboratorios
    public static final String LAB_READ = AUTHENTICATED;
    public static final String LAB_WRITE = ADMIN_ONLY;

    // Equipos
    public static final String EQUIPO_READ = AUTHENTICATED;
    public static final String EQUIPO_WRITE = ADMIN_ONLY;

    // Registros de Uso
    public static final String REGISTRO_ENTRADA_SALIDA = DOCENTES;
    public static final String REGISTRO_ADMIN = ADMIN_ONLY;
    public static final String REGISTRO_READ = DIRECTORES;

    // Novedades
    public static final String NOVEDAD_CREATE = USUARIOS_REGISTRADOS;
    public static final String NOVEDAD_CHANGE_STATUS = "hasAnyRole('ROLE_USUARIO', 'ROLE_ADMIN')";  // Solo USUARIO y ADMIN
    public static final String NOVEDAD_UPLOAD_IMAGES = "hasAnyRole('ROLE_USUARIO', 'ROLE_ADMIN')";  // Solo USUARIO y ADMIN para subir fotos
    public static final String NOVEDAD_READ = AUTHENTICATED;

    // Usuarios
    public static final String USUARIO_ME = AUTHENTICATED;
    public static final String USUARIO_ADMIN = ADMIN_ONLY;

    // Auditoría
    public static final String AUDITORIA = DIRECTORES;
}

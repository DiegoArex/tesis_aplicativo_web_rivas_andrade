-- ============================================
-- SCRIPT DE CREACIÓN DE TABLAS
-- Sistema de Gestión de Laboratorios
-- ============================================
-- Nota: Este script SOLO crea las tablas
-- SIN insertar datos de ejemplo
-- ============================================

-- Tabla: USUARIOS
-- ============================================
CREATE TABLE usuarios (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(100) UNIQUE NOT NULL,
    email VARCHAR(150),
    keycloak_id VARCHAR(100) UNIQUE,
    nombre_completo VARCHAR(200) NOT NULL,
    roles VARCHAR(500),
    carrera VARCHAR(100),
    facultad VARCHAR(100),
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    deleted_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Índices para usuarios
CREATE INDEX idx_usuario_activo ON usuarios(activo);
CREATE INDEX idx_usuario_carrera ON usuarios(carrera);
CREATE INDEX idx_usuario_facultad ON usuarios(facultad);
CREATE INDEX idx_usuario_created_at ON usuarios(created_at);
CREATE INDEX idx_usuario_deleted_at ON usuarios(deleted_at);


-- Tabla: LABORATORIOS
-- ============================================
CREATE TABLE laboratorios (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(100) UNIQUE NOT NULL,
    ubicacion VARCHAR(200) NOT NULL,
    capacidad INTEGER NOT NULL,
    descripcion VARCHAR(500),
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    deleted_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Índices para laboratorios
CREATE INDEX idx_laboratorio_activo ON laboratorios(activo);
CREATE INDEX idx_laboratorio_created_at ON laboratorios(created_at);
CREATE INDEX idx_laboratorio_deleted_at ON laboratorios(deleted_at);


-- Tabla: EQUIPOS
-- ============================================
CREATE TABLE equipos (
    id BIGSERIAL PRIMARY KEY,
    codigo VARCHAR(50) UNIQUE NOT NULL,
    tipo VARCHAR(50) NOT NULL,
    marca VARCHAR(100),
    modelo VARCHAR(100),
    numero_serie VARCHAR(100),
    estado VARCHAR(50) NOT NULL,
    laboratorio_id BIGINT NOT NULL,
    deleted_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_equipo_laboratorio FOREIGN KEY (laboratorio_id) REFERENCES laboratorios(id) ON DELETE CASCADE
);

-- Índices para equipos
CREATE INDEX idx_equipo_estado ON equipos(estado);
CREATE INDEX idx_equipo_tipo ON equipos(tipo);
CREATE INDEX idx_equipo_laboratorio ON equipos(laboratorio_id);
CREATE INDEX idx_equipo_created_at ON equipos(created_at);
CREATE INDEX idx_equipo_deleted_at ON equipos(deleted_at);


-- Tabla: REGISTROS_USO
-- ============================================
CREATE TABLE registros_uso (
    id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT NOT NULL,
    laboratorio_id BIGINT NOT NULL,
    laboratorio_secundario_id BIGINT,
    fecha_entrada TIMESTAMP NOT NULL,
    fecha_salida TIMESTAMP,
    proposito VARCHAR(100) NOT NULL,
    observaciones VARCHAR(500),
    tipo_registro VARCHAR(20) DEFAULT 'NORMAL',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_registro_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE,
    CONSTRAINT fk_registro_laboratorio FOREIGN KEY (laboratorio_id) REFERENCES laboratorios(id) ON DELETE CASCADE,
    CONSTRAINT fk_laboratorio_secundario FOREIGN KEY (laboratorio_secundario_id) REFERENCES laboratorios(id) ON DELETE SET NULL
);

-- Índices para registros_uso
CREATE INDEX idx_registro_usuario ON registros_uso(usuario_id);
CREATE INDEX idx_registro_laboratorio ON registros_uso(laboratorio_id);
CREATE INDEX idx_registro_fecha_entrada ON registros_uso(fecha_entrada);
CREATE INDEX idx_registro_fecha_salida ON registros_uso(fecha_salida);
CREATE INDEX idx_registro_tipo ON registros_uso(tipo_registro);
CREATE INDEX idx_registro_lab_secundario ON registros_uso(laboratorio_secundario_id);
CREATE INDEX idx_registro_lab_fecha ON registros_uso(laboratorio_id, fecha_entrada);


-- Tabla: NOVEDADES
-- ============================================
CREATE TABLE novedades (
    id BIGSERIAL PRIMARY KEY,
    titulo VARCHAR(200) NOT NULL,
    descripcion VARCHAR(2000) NOT NULL,
    tipo VARCHAR(20) NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    prioridad VARCHAR(20) NOT NULL,
    usuario_reporta_id BIGINT NOT NULL,
    laboratorio_id BIGINT NOT NULL,
    equipo_id BIGINT,
    fecha_reporte TIMESTAMP NOT NULL,
    fecha_resolucion TIMESTAMP,
    observaciones_resolucion VARCHAR(1000),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_novedad_usuario FOREIGN KEY (usuario_reporta_id) REFERENCES usuarios(id) ON DELETE CASCADE,
    CONSTRAINT fk_novedad_laboratorio FOREIGN KEY (laboratorio_id) REFERENCES laboratorios(id) ON DELETE CASCADE,
    CONSTRAINT fk_novedad_equipo FOREIGN KEY (equipo_id) REFERENCES equipos(id) ON DELETE SET NULL
);

-- Índices para novedades
CREATE INDEX idx_novedad_estado ON novedades(estado);
CREATE INDEX idx_novedad_tipo ON novedades(tipo);
CREATE INDEX idx_novedad_prioridad ON novedades(prioridad);
CREATE INDEX idx_novedad_fecha_reporte ON novedades(fecha_reporte);
CREATE INDEX idx_novedad_usuario ON novedades(usuario_reporta_id);
CREATE INDEX idx_novedad_laboratorio ON novedades(laboratorio_id);
CREATE INDEX idx_novedad_equipo ON novedades(equipo_id);
CREATE INDEX idx_novedad_estado_prioridad ON novedades(estado, prioridad);


-- Tabla: IMAGENES_NOVEDAD
-- ============================================
CREATE TABLE imagenes_novedad (
    id BIGSERIAL PRIMARY KEY,
    novedad_id BIGINT NOT NULL,
    nombre_archivo VARCHAR(255) NOT NULL,
    tipo_mime VARCHAR(100) NOT NULL,
    imagen_base64 TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_imagen_novedad FOREIGN KEY (novedad_id) REFERENCES novedades(id) ON DELETE CASCADE
);

-- Índice para imagenes_novedad
CREATE INDEX idx_imagen_novedad ON imagenes_novedad(novedad_id);


-- ============================================
-- FIN DEL SCRIPT DE CREACIÓN DE TABLAS
-- ============================================

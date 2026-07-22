package com.example.demo.enums;

/**
 * Enum para diferenciar entre registros normales y exámenes
 */
public enum TipoRegistro {
    NORMAL("Normal", "Uso regular del laboratorio"),
    EXAMEN("Examen", "Uso para examen (puede usar múltiples laboratorios)");

    private final String nombre;
    private final String descripcion;

    TipoRegistro(String nombre, String descripcion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }
}

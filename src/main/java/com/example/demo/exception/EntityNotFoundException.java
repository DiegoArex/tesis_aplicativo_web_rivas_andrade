package com.example.demo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Excepción personalizada para cuando una entidad no es encontrada en la base de datos.
 * Reemplaza los RuntimeException genéricos con un manejo más específico y consistente.
 * 
 * Uso:
 *   throw new EntityNotFoundException("Usuario", 123L);
 *   throw new EntityNotFoundException("Laboratorio", "LAB-001");
 * 
 * Retorna HTTP 404 automáticamente.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class EntityNotFoundException extends RuntimeException {
    
    private final String entityName;
    private final Object identifier;
    
    /**
     * Constructor para EntityNotFoundException
     * 
     * @param entityName Nombre de la entidad (ej: "Usuario", "Laboratorio", "Equipo")
     * @param identifier Identificador de la entidad (ej: ID, nombre, email)
     */
    public EntityNotFoundException(String entityName, Object identifier) {
        super(String.format("%s no encontrado: %s", entityName, identifier));
        this.entityName = entityName;
        this.identifier = identifier;
    }
    
    /**
     * Constructor alternativo para casos donde no hay identificador específico
     * 
     * @param entityName Nombre de la entidad
     */
    public EntityNotFoundException(String entityName) {
        super(String.format("%s no encontrado", entityName));
        this.entityName = entityName;
        this.identifier = null;
    }
    
    public String getEntityName() {
        return entityName;
    }
    
    public Object getIdentifier() {
        return identifier;
    }
}

package com.example.demo.annotation;

import java.lang.annotation.*;

/**
 * Anotación para inyectar automáticamente el username del usuario autenticado.
 * 
 * Simplifica controladores eliminando:
 *   Authentication authentication
 *   authentication.getName()
 * 
 * Uso:
 *   public String miMetodo(@CurrentUser String username) { ... }
 * 
 * En lugar de:
 *   public String miMetodo(Authentication authentication) {
 *       String username = authentication.getName();
 *   }
 * 
 * Beneficios:
 * - ✅ Código más limpio
 * - ✅ Menos parámetros en métodos
 * - ✅ Mejor legibilidad
 * - ✅ Consistencia en toda la aplicación
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CurrentUser {
}

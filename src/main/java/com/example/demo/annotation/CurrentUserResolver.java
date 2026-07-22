package com.example.demo.annotation;

import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * Resolver para la anotación @CurrentUser.
 * 
 * Extrae automáticamente el username del usuario autenticado desde Spring Security
 * y lo inyecta en parámetros de métodos anotados con @CurrentUser.
 * 
 * Flujo:
 * 1. Detecta parámetro anotado con @CurrentUser
 * 2. Obtiene el contexto de seguridad de Spring Security
 * 3. Extrae el nombre de usuario (username)
 * 4. Inyecta automáticamente en el parámetro del método
 * 
 * Ejemplo:
 *   @GetMapping
 *   public String miMetodo(@CurrentUser String username) {
 *       // username contiene el nombre del usuario autenticado
 *   }
 */
@Component
@RequiredArgsConstructor
public class CurrentUserResolver implements HandlerMethodArgumentResolver {

    /**
     * Verifica si este resolver puede procesar el parámetro.
     * 
     * @param parameter Parámetro del método
     * @return true si el parámetro está anotado con @CurrentUser y es String
     */
    @Override
    public boolean supportsParameter(@NonNull MethodParameter parameter) {
        return parameter.getParameterAnnotation(CurrentUser.class) != null &&
               parameter.getParameterType().equals(String.class);
    }

    /**
     * Resuelve el valor del parámetro anotado con @CurrentUser.
     * 
     * @param parameter Parámetro del método
     * @param mavContainer Spring MVC container
     * @param webRequest Request web actual
     * @param binderFactory Factory para binders
     * @return El username del usuario autenticado, o null si no hay autenticación
     */
    @Override
    @Nullable
    public Object resolveArgument(@NonNull MethodParameter parameter,
                                 @Nullable ModelAndViewContainer mavContainer,
                                 @NonNull NativeWebRequest webRequest,
                                 @Nullable WebDataBinderFactory binderFactory) {
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        
        return authentication.getName();
    }
}

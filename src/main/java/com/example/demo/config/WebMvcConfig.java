package com.example.demo.config;

import com.example.demo.annotation.CurrentUserResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * Configuración de Spring Web MVC.
 * 
 * Registra los resolvers personalizados para anotaciones como @CurrentUser.
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {
    
    private final CurrentUserResolver currentUserResolver;
    
    /**
     * Registra los argument resolvers personalizados.
     * 
     * @param resolvers Lista de resolvers
     */
    @Override
    public void addArgumentResolvers(@NonNull List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(currentUserResolver);
    }
}

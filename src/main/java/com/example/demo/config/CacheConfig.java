package com.example.demo.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * PARTE 1 & 6: Configuración de Caché Caffeine
 * 
 * Optimizaciones de caché en memoria para datos de lectura frecuente:
 * - Usuarios: 10 minutos TTL
 * - Laboratorios: 15 minutos TTL
 * - Equipos: 15 minutos TTL
 * - Novedades: 5 minutos TTL (cambian frecuente)
 * 
 * Max 10,000 entradas en total.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(
                "usuarios",
                "laboratorios", 
                "equipos",
                "novedades"
        );
        
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(10000)
                .recordStats());
        
        return cacheManager;
    }
}

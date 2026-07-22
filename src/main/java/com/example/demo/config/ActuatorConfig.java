package com.example.demo.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * PARTE 1: Configuración de Actuator y Métricas Prometheus
 * 
 * Proporciona visibilidad en:
 * - Health checks de la aplicación
 * - Métricas JVM (memoria, CPU, threads)
 * - Métricas de proceso
 * - Métricas personalizadas
 * 
 * Accesible en: http://localhost:8080/actuator/metrics
 * Prometheus endpoint: http://localhost:8080/actuator/prometheus
 */
@Configuration
public class ActuatorConfig {

    @Bean
    public MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
        return registry -> registry.config()
                .commonTags(
                        "application", "demo-laboratorios",
                        "version", "3.2.5"
                );
    }

    /**
     * Métrica personalizada: Tiempo de respuesta en controladores
     * MeterRegistry se inyecta automáticamente por Spring
     */
    @Bean
    public Timer controllerResponseTime(MeterRegistry registry) {
        return Timer.builder("http.request.duration")
                .description("Tiempo de respuesta de requests HTTP")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(registry);
    }
}


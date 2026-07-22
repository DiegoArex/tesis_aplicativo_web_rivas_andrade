package com.example.demo.service;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Servicio centralizado para mapeo de entidades a DTOs.
 * 
 * Consolida la lógica repetida de:
 *   list.stream().map(mapper).collect(Collectors.toList())
 * 
 * Proporciona métodos genéricos reutilizables en todos los servicios.
 * 
 * Beneficios:
 * - ✅ Código DRY (Don't Repeat Yourself)
 * - ✅ Menos líneas de código duplicado
 * - ✅ Consistencia en toda la aplicación
 * - ✅ Fácil de mantener y actualizar
 * 
 * Ejemplos de uso:
 * 
 * // Mapear lista simple
 * List<NovedadResponseDTO> dtos = mapperService.mapList(
 *     novedadRepository.findAll(),
 *     this::convertirAResponseDTO
 * );
 * 
 * // Mapear página
 * Page<NovedadResponseDTO> dtosPage = mapperService.mapPage(
 *     novedadRepository.findAll(pageable),
 *     this::convertirAResponseDTO
 * );
 * 
 * // Mapear con filtro
 * List<NovedadResponseDTO> dtosFiltradas = mapperService.mapListFiltered(
 *     novedadRepository.findAll(),
 *     n -> n.getEstado() == EstadoNovedad.PENDIENTE,
 *     this::convertirAResponseDTO
 * );
 */
@Service
public class MapperService {
    
    /**
     * Mapea una lista de entidades a una lista de DTOs.
     * 
     * Reemplaza el patrón común:
     *   list.stream().map(mapper).collect(Collectors.toList())
     * 
     * @param <T> Tipo de la entidad
     * @param <D> Tipo del DTO
     * @param entities Lista de entidades
     * @param mapper Función para convertir Entity -> DTO
     * @return Lista de DTOs mapeados
     * 
     * @example
     * List<NovedadResponseDTO> dtos = mapperService.mapList(
     *     novedadRepository.findAll(),
     *     this::convertirAResponseDTO
     * );
     */
    public <T, D> List<D> mapList(List<T> entities, Function<T, D> mapper) {
        if (entities == null || entities.isEmpty()) {
            return List.of();
        }
        
        return entities.stream()
                .map(mapper)
                .collect(Collectors.toList());
    }
    
    /**
     * Mapea una página de entidades a una página de DTOs.
     * 
     * Útil para endpoints paginados.
     * 
     * @param <T> Tipo de la entidad
     * @param <D> Tipo del DTO
     * @param entities Página de entidades
     * @param mapper Función para convertir Entity -> DTO
     * @return Página de DTOs mapeados
     * 
     * @example
     * Page<NovedadResponseDTO> dtos = mapperService.mapPage(
     *     novedadRepository.findAll(pageable),
     *     this::convertirAResponseDTO
     * );
     */
    public <T, D> Page<D> mapPage(Page<T> entities, Function<T, D> mapper) {
        if (entities == null || !entities.hasContent()) {
            return Page.empty();
        }
        
        return entities.map(mapper);
    }
    
    /**
     * Mapea una lista de entidades con filtro previo.
     * 
     * Útil cuando necesitas filtrar antes de mapear.
     * 
     * @param <T> Tipo de la entidad
     * @param <D> Tipo del DTO
     * @param entities Lista de entidades
     * @param filter Predicado para filtrar
     * @param mapper Función para convertir Entity -> DTO
     * @return Lista de DTOs filtrados y mapeados
     * 
     * @example
     * List<NovedadResponseDTO> pendientes = mapperService.mapListFiltered(
     *     novedadRepository.findAll(),
     *     n -> n.getEstado() == EstadoNovedad.PENDIENTE,
     *     this::convertirAResponseDTO
     * );
     */
    public <T, D> List<D> mapListFiltered(List<T> entities, 
                                          Predicate<T> filter, 
                                          Function<T, D> mapper) {
        if (entities == null || entities.isEmpty()) {
            return List.of();
        }
        
        return entities.stream()
                .filter(filter)
                .map(mapper)
                .collect(Collectors.toList());
    }
    
    /**
     * Mapea una lista de entidades con múltiples filtros.
     * 
     * Útil para filtrados más complejos.
     * 
     * @param <T> Tipo de la entidad
     * @param <D> Tipo del DTO
     * @param entities Lista de entidades
     * @param filters Predicados para filtrar (se aplican con AND)
     * @param mapper Función para convertir Entity -> DTO
     * @return Lista de DTOs filtrados y mapeados
     * 
     * @example
     * List<RegistroUsoResponseDTO> registros = mapperService.mapListFiltered(
     *     registroUsoRepository.findAll(),
     *     r -> r.getFechaSalida() != null,
     *     r -> r.getUsuario().getId() == usuarioId,
     *     this::convertirAResponseDTO
     * );
     */
    @SafeVarargs
    public final <T, D> List<D> mapListFilteredMulti(List<T> entities, 
                                                     Function<T, D> mapper,
                                                     Predicate<T>... filters) {
        if (entities == null || entities.isEmpty()) {
            return List.of();
        }
        
        // Combinar todos los filtros con AND
        Predicate<T> combinedFilter = t -> true;
        for (Predicate<T> filter : filters) {
            combinedFilter = combinedFilter.and(filter);
        }
        
        return entities.stream()
                .filter(combinedFilter)
                .map(mapper)
                .collect(Collectors.toList());
    }
    
    /**
     * Mapea una lista de entidades y las ordena por un comparador.
     * 
     * @param <T> Tipo de la entidad
     * @param <D> Tipo del DTO
     * @param entities Lista de entidades
     * @param mapper Función para convertir Entity -> DTO
     * @param comparator Comparador para ordenar
     * @return Lista de DTOs mapeados y ordenados
     * 
     * @example
     * List<NovedadResponseDTO> orderedByDate = mapperService.mapListSorted(
     *     novedadRepository.findAll(),
     *     this::convertirAResponseDTO,
     *     Comparator.comparing(Novedad::getFechaReporte).reversed()
     * );
     */
    public <T, D> List<D> mapListSorted(List<T> entities, 
                                        Function<T, D> mapper,
                                        java.util.Comparator<T> comparator) {
        if (entities == null || entities.isEmpty()) {
            return List.of();
        }
        
        return entities.stream()
                .sorted(comparator)
                .map(mapper)
                .collect(Collectors.toList());
    }
}

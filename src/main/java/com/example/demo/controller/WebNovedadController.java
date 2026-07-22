package com.example.demo.controller;

import com.example.demo.annotation.CurrentUser;
import com.example.demo.dto.CambiarEstadoNovedadDTO;
import com.example.demo.dto.EquipoResponseDTO;
import com.example.demo.dto.ImagenNovedadRequestDTO;
import com.example.demo.dto.ImagenNovedadResponseDTO;
import com.example.demo.dto.LaboratorioResponseDTO;
import com.example.demo.dto.NovedadRequestDTO;
import com.example.demo.dto.NovedadResponseDTO;
import com.example.demo.enums.EstadoNovedad;
import com.example.demo.enums.TipoNovedad;
import com.example.demo.security.PermissionConstants;
import com.example.demo.service.EquipoService;
import com.example.demo.service.LaboratorioService;
import com.example.demo.service.NovedadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Set;

@Slf4j
@Controller
@RequestMapping("/web/novedades")
@RequiredArgsConstructor
public class WebNovedadController {

    private static final long MAX_IMAGE_SIZE_BYTES = 4L * 1024 * 1024; // 4 MB
    private static final Set<String> EXTENSIONES_VALIDAS = Set.of("jpg", "jpeg", "png");
    private static final Set<String> MIME_TYPES_VALIDOS = Set.of("image/jpeg", "image/png");

    private final NovedadService novedadService;
    private final LaboratorioService laboratorioService;
    private final EquipoService equipoService;

    // ─────────────────────────────────────────────────────────────
    // LISTADOS
    // ─────────────────────────────────────────────────────────────

    @GetMapping
    @PreAuthorize(PermissionConstants.AUTHENTICATED)
    public String listarNovedades(Model model) {
        List<NovedadResponseDTO> novedades = novedadService.listarTodas();
        model.addAttribute("novedades", novedades);
        model.addAttribute("titulo", "Novedades");
        cargarDatosModal(model);
        return "novedades/lista";
    }

    @GetMapping("/mis")
    @PreAuthorize(PermissionConstants.AUTHENTICATED)
    public String listarMisNovedades(@CurrentUser String username, Model model) {
        List<NovedadResponseDTO> novedades = novedadService.listarMisNovedades(username);
        model.addAttribute("novedades", novedades);
        model.addAttribute("titulo", "Mis Novedades");
        model.addAttribute("filtro", "mis");
        cargarDatosModal(model);
        return "novedades/lista";
    }

    @GetMapping("/estado/{estado}")
    @PreAuthorize(PermissionConstants.AUTHENTICATED)
    public String listarPorEstado(@PathVariable EstadoNovedad estado, Model model) {
        List<NovedadResponseDTO> novedades = novedadService.listarPorEstado(estado);
        model.addAttribute("novedades", novedades);
        model.addAttribute("titulo", "Novedades - Estado: " + estado.name());
        model.addAttribute("estado", estado);
        cargarDatosModal(model);
        return "novedades/lista";
    }

    @GetMapping("/carrera/{carrera}")
    @PreAuthorize(PermissionConstants.AUTHENTICATED)
    public String listarPorCarrera(@PathVariable String carrera, Model model) {
        List<NovedadResponseDTO> novedades = novedadService.listarPorCarrera(carrera);
        model.addAttribute("novedades", novedades);
        model.addAttribute("titulo", "Novedades por Carrera");
        model.addAttribute("carrera", carrera);
        cargarDatosModal(model);
        return "novedades/lista";
    }

    // ─────────────────────────────────────────────────────────────
    // AJAX: Equipos por laboratorio
    // ─────────────────────────────────────────────────────────────

    @GetMapping("/equipos/{labId}")
    @PreAuthorize(PermissionConstants.AUTHENTICATED)
    @ResponseBody
    public List<EquipoResponseDTO> equiposPorLaboratorio(@PathVariable Long labId) {
        try {
            return equipoService.listarPorLaboratorio(labId);
        } catch (Exception e) {
            log.warn("Error al cargar equipos para lab {}: {}", labId, e.getMessage());
            return new ArrayList<>();
        }
    }

    // ─────────────────────────────────────────────────────────────
    // DETALLE
    // ─────────────────────────────────────────────────────────────

    @GetMapping("/{id}")
    @PreAuthorize(PermissionConstants.AUTHENTICATED)
    public String verNovedad(@PathVariable Long id, Model model) {
        NovedadResponseDTO novedad = novedadService.obtenerPorId(id);
        List<ImagenNovedadResponseDTO> imagenes = novedadService.obtenerImagenes(id);
        model.addAttribute("novedad", novedad);
        model.addAttribute("imagenes", imagenes);
        model.addAttribute("cambiarEstadoDTO", new CambiarEstadoNovedadDTO());
        model.addAttribute("titulo", "Detalle de Novedad");
        return "novedades/detalle";
    }

    // ─────────────────────────────────────────────────────────────
    // CREAR NOVEDAD
    // ─────────────────────────────────────────────────────────────

    @GetMapping("/nueva")
    @PreAuthorize(PermissionConstants.NOVEDAD_CREATE)
    public String mostrarFormularioNueva(Model model) {
        NovedadRequestDTO novedadDTO = new NovedadRequestDTO();
        novedadDTO.setTipo(TipoNovedad.GENERAL);
        novedadDTO.setPrioridad("MEDIA");
        novedadDTO.setEstado(EstadoNovedad.PENDIENTE);
        cargarDatosFormulario(model, novedadDTO, "Reportar Novedad", "Reportar");
        return "novedades/formulario";
    }

    @PostMapping
    @PreAuthorize(PermissionConstants.NOVEDAD_CREATE)
    public String crearNovedad(
            @Valid @ModelAttribute("novedad") NovedadRequestDTO novedadDTO,
            BindingResult result,
            @RequestParam(required = false) MultipartFile archivo,
            RedirectAttributes redirectAttributes,
            Model model,
            @CurrentUser String username) {

        if (novedadDTO.getTipo() == TipoNovedad.GENERAL) {
            novedadDTO.setEquipoId(null);
        }

        if (novedadDTO.getTipo() == TipoNovedad.EQUIPO && novedadDTO.getEquipoId() == null) {
            result.rejectValue("equipoId", "equipoId.required",
                    "Debe seleccionar un equipo para novedades de tipo EQUIPO");
        }

        if (archivo != null && !archivo.isEmpty()) {
            String errorImagen = validarImagen(archivo);
            if (errorImagen != null) {
                model.addAttribute("error", errorImagen);
                cargarDatosFormulario(model, novedadDTO, "Reportar Novedad", "Reportar");
                return "novedades/formulario";
            }
        }

        if (result.hasErrors()) {
            log.warn("Formulario de novedad con errores de validación: {}", result.getAllErrors());
            cargarDatosFormulario(model, novedadDTO, "Reportar Novedad", "Reportar");
            return "novedades/formulario";
        }

        try {
            NovedadResponseDTO creada = novedadService.reportarNovedad(username, novedadDTO);
            log.info("Novedad creada con ID {} por usuario {}", creada.getId(), username);

            if (archivo != null && !archivo.isEmpty()) {
                try {
                    ImagenNovedadRequestDTO imagenDTO = new ImagenNovedadRequestDTO();
                    imagenDTO.setNombreArchivo(archivo.getOriginalFilename());
                    imagenDTO.setTipoMime(archivo.getContentType());
                    imagenDTO.setImagenBase64(Base64.getEncoder().encodeToString(archivo.getBytes()));
                    novedadService.adjuntarImagen(creada.getId(), username, imagenDTO);
                    log.info("Imagen adjuntada a novedad ID {}", creada.getId());
                } catch (Exception e) {
                    log.warn("Novedad creada pero error al adjuntar imagen: {}", e.getMessage());
                    redirectAttributes.addFlashAttribute("mensaje",
                            "Novedad creada con ID #" + creada.getId() + ", pero hubo un error al adjuntar la imagen.");
                    redirectAttributes.addFlashAttribute("tipo", "warning");
                    return "redirect:/web/novedades/" + creada.getId();
                }
            }

            redirectAttributes.addFlashAttribute("mensaje", "Novedad reportada exitosamente con ID #" + creada.getId());
            redirectAttributes.addFlashAttribute("tipo", "success");
            return "redirect:/web/novedades/" + creada.getId();
        } catch (Exception e) {
            log.error("Error al crear novedad para usuario {}: {}", username, e.getMessage(), e);
            model.addAttribute("error", "Error al reportar novedad: " + e.getMessage());
            cargarDatosFormulario(model, novedadDTO, "Reportar Novedad", "Reportar");
            return "novedades/formulario";
        }
    }

    // ─────────────────────────────────────────────────────────────
    // CAMBIAR ESTADO
    // ─────────────────────────────────────────────────────────────

    @PostMapping("/{id}/cambiar-estado")
    @PreAuthorize(PermissionConstants.NOVEDAD_CHANGE_STATUS)
    public String cambiarEstado(
            @PathVariable Long id,
            @CurrentUser String username,
            @ModelAttribute CambiarEstadoNovedadDTO cambiarEstadoDTO,
            RedirectAttributes redirectAttributes) {
        try {
            novedadService.cambiarEstado(id, username, cambiarEstadoDTO);
            redirectAttributes.addFlashAttribute("mensaje", "Estado actualizado exitosamente");
            redirectAttributes.addFlashAttribute("tipo", "success");
        } catch (Exception e) {
            log.error("Error al cambiar estado de novedad {}: {}", id, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("mensaje", "Error al cambiar estado: " + e.getMessage());
            redirectAttributes.addFlashAttribute("tipo", "danger");
        }
        return "redirect:/web/novedades/" + id;
    }

    // ─────────────────────────────────────────────────────────────
    // ADJUNTAR IMAGEN
    // ─────────────────────────────────────────────────────────────

    @PostMapping("/{id}/imagen")
    @PreAuthorize(PermissionConstants.NOVEDAD_UPLOAD_IMAGES)
    public String adjuntarImagen(
            @PathVariable Long id,
            @RequestParam MultipartFile archivo,
            @CurrentUser String username,
            RedirectAttributes redirectAttributes) {

        if (archivo == null || archivo.isEmpty()) {
            redirectAttributes.addFlashAttribute("mensaje", "Debe seleccionar una imagen válida");
            redirectAttributes.addFlashAttribute("tipo", "danger");
            return "redirect:/web/novedades/" + id;
        }

        String errorImagen = validarImagen(archivo);
        if (errorImagen != null) {
            redirectAttributes.addFlashAttribute("mensaje", errorImagen);
            redirectAttributes.addFlashAttribute("tipo", "danger");
            return "redirect:/web/novedades/" + id;
        }

        try {
            ImagenNovedadRequestDTO requestDTO = new ImagenNovedadRequestDTO();
            requestDTO.setNombreArchivo(archivo.getOriginalFilename());
            requestDTO.setTipoMime(archivo.getContentType());
            requestDTO.setImagenBase64(Base64.getEncoder().encodeToString(archivo.getBytes()));

            novedadService.adjuntarImagen(id, username, requestDTO);
            redirectAttributes.addFlashAttribute("mensaje", "Imagen adjuntada exitosamente");
            redirectAttributes.addFlashAttribute("tipo", "success");
        } catch (Exception e) {
            log.error("Error al adjuntar imagen a novedad {}: {}", id, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("mensaje", "Error al adjuntar imagen: " + e.getMessage());
            redirectAttributes.addFlashAttribute("tipo", "danger");
        }

        return "redirect:/web/novedades/" + id;
    }

    // ─────────────────────────────────────────────────────────────
    // HELPERS PRIVADOS
    // ─────────────────────────────────────────────────────────────

    /**
     * Valida tipo MIME, extensión y tamaño de la imagen.
     * Retorna null si es válida, o un mensaje de error si no lo es.
     */
    private String validarImagen(MultipartFile archivo) {
        String contentType = archivo.getContentType();
        if (contentType == null || !MIME_TYPES_VALIDOS.contains(contentType)) {
            return "Solo se permiten imágenes en formato JPG o PNG.";
        }

        String nombreOriginal = archivo.getOriginalFilename();
        if (nombreOriginal != null && nombreOriginal.contains(".")) {
            String ext = nombreOriginal.substring(nombreOriginal.lastIndexOf('.') + 1).toLowerCase();
            if (!EXTENSIONES_VALIDAS.contains(ext)) {
                return "Solo se permiten archivos con extensión .jpg, .jpeg o .png.";
            }
        }

        if (archivo.getSize() > MAX_IMAGE_SIZE_BYTES) {
            double pesoMB = archivo.getSize() / (1024.0 * 1024.0);
            return String.format("La imagen no puede superar los 4 MB. El archivo seleccionado pesa %.1f MB.", pesoMB);
        }

        return null;
    }

    private void cargarDatosFormulario(Model model, NovedadRequestDTO novedadDTO,
            String titulo, String accion) {
        model.addAttribute("novedad", novedadDTO);
        model.addAttribute("laboratorios", obtenerLaboratoriosSeguros());
        model.addAttribute("equipos", obtenerEquiposSeguros());
        model.addAttribute("titulo", titulo);
        model.addAttribute("accion", accion);
        model.addAttribute("tiposNovedad", TipoNovedad.values());
        model.addAttribute("estadosNovedad", EstadoNovedad.values());
    }

    private void cargarDatosModal(Model model) {
        model.addAttribute("laboratorios", obtenerLaboratoriosSeguros());
        model.addAttribute("equipos", obtenerEquiposSeguros());
    }

    private List<LaboratorioResponseDTO> obtenerLaboratoriosSeguros() {
        try {
            List<LaboratorioResponseDTO> labs = laboratorioService.listarTodos();
            return labs != null ? labs : new ArrayList<>();
        } catch (Exception e) {
            log.warn("No se pudieron cargar laboratorios: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    private List<EquipoResponseDTO> obtenerEquiposSeguros() {
        try {
            List<EquipoResponseDTO> equipos = equipoService.listarTodos();
            return equipos != null ? equipos : new ArrayList<>();
        } catch (Exception e) {
            log.warn("No se pudieron cargar equipos: {}", e.getMessage());
            return new ArrayList<>();
        }
    }
}

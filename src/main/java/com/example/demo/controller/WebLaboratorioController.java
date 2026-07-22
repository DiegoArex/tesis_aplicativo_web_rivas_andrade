package com.example.demo.controller;

import com.example.demo.dto.LaboratorioRequestDTO;
import com.example.demo.dto.LaboratorioResponseDTO;
import com.example.demo.security.PermissionConstants;
import com.example.demo.service.LaboratorioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Controlador Web para gestión de Laboratorios con Thymeleaf
 */
@Controller
@RequestMapping("/web/laboratorios")
@RequiredArgsConstructor
public class WebLaboratorioController {

    private final LaboratorioService laboratorioService;

    @GetMapping
    @PreAuthorize(PermissionConstants.AUTHENTICATED)
    public String listarLaboratorios(Model model) {
        List<LaboratorioResponseDTO> laboratorios = laboratorioService.listarTodos();
        model.addAttribute("laboratorios", laboratorios);
        model.addAttribute("titulo", "Laboratorios");
        return "laboratorios/lista";
    }

    @GetMapping("/activos")
    @PreAuthorize(PermissionConstants.AUTHENTICATED)
    public String listarActivos(Model model) {
        List<LaboratorioResponseDTO> laboratorios = laboratorioService.listarActivosNoEliminados();
        model.addAttribute("laboratorios", laboratorios);
        model.addAttribute("titulo", "Laboratorios Activos");
        model.addAttribute("filtro", "activos");
        return "laboratorios/lista";
    }

    @GetMapping("/eliminados")
    @PreAuthorize(PermissionConstants.LAB_WRITE)
    public String listarEliminados(Model model) {
        List<LaboratorioResponseDTO> laboratorios = laboratorioService.listarEliminados();
        model.addAttribute("laboratorios", laboratorios);
        model.addAttribute("titulo", "Laboratorios Eliminados");
        model.addAttribute("filtro", "eliminados");
        return "laboratorios/eliminados";
    }

    @GetMapping("/{id}")
    @PreAuthorize(PermissionConstants.AUTHENTICATED)
    public String verLaboratorio(@PathVariable Long id, Model model) {
        LaboratorioResponseDTO laboratorio = laboratorioService.obtenerPorId(id);
        model.addAttribute("laboratorio", laboratorio);
        model.addAttribute("titulo", "Detalle de Laboratorio");
        return "laboratorios/detalle";
    }

    @GetMapping("/nuevo")
    @PreAuthorize(PermissionConstants.LAB_WRITE)
    public String mostrarFormularioNuevo(Model model) {
        model.addAttribute("laboratorio", new LaboratorioRequestDTO());
        model.addAttribute("titulo", "Nuevo Laboratorio");
        model.addAttribute("accion", "Crear");
        return "laboratorios/formulario";
    }

    @PostMapping
    @PreAuthorize(PermissionConstants.LAB_WRITE)
    public String crearLaboratorio(@Valid @ModelAttribute("laboratorio") LaboratorioRequestDTO laboratorioDTO,
                                  BindingResult result,
                                  RedirectAttributes redirectAttributes,
                                  Model model) {
        if (result.hasErrors()) {
            model.addAttribute("titulo", "Nuevo Laboratorio");
            model.addAttribute("accion", "Crear");
            return "laboratorios/formulario";
        }

        try {
            LaboratorioResponseDTO creado = laboratorioService.crear(laboratorioDTO);
            redirectAttributes.addFlashAttribute("mensaje", "Laboratorio creado exitosamente");
            redirectAttributes.addFlashAttribute("tipo", "success");
            return "redirect:/web/laboratorios/" + creado.getId();
        } catch (Exception e) {
            model.addAttribute("error", "Error al crear laboratorio: " + e.getMessage());
            model.addAttribute("titulo", "Nuevo Laboratorio");
            model.addAttribute("accion", "Crear");
            return "laboratorios/formulario";
        }
    }

    @GetMapping("/{id}/editar")
    @PreAuthorize(PermissionConstants.LAB_WRITE)
    public String mostrarFormularioEditar(@PathVariable Long id, Model model) {
        LaboratorioResponseDTO laboratorio = laboratorioService.obtenerPorId(id);
        
        LaboratorioRequestDTO laboratorioRequest = new LaboratorioRequestDTO();
        laboratorioRequest.setNombre(laboratorio.getNombre());
        laboratorioRequest.setUbicacion(laboratorio.getUbicacion());
        laboratorioRequest.setCapacidad(laboratorio.getCapacidad());
        
        model.addAttribute("laboratorio", laboratorioRequest);
        model.addAttribute("laboratorioId", id);
        model.addAttribute("titulo", "Editar Laboratorio");
        model.addAttribute("accion", "Actualizar");
        return "laboratorios/formulario";
    }

    @PostMapping("/{id}/editar")
    @PreAuthorize(PermissionConstants.LAB_WRITE)
    public String actualizarLaboratorio(@PathVariable Long id,
                                       @Valid @ModelAttribute("laboratorio") LaboratorioRequestDTO laboratorioDTO,
                                       BindingResult result,
                                       RedirectAttributes redirectAttributes,
                                       Model model) {
        if (result.hasErrors()) {
            model.addAttribute("laboratorioId", id);
            model.addAttribute("titulo", "Editar Laboratorio");
            model.addAttribute("accion", "Actualizar");
            return "laboratorios/formulario";
        }

        try {
            laboratorioService.actualizar(id, laboratorioDTO);
            redirectAttributes.addFlashAttribute("mensaje", "Laboratorio actualizado exitosamente");
            redirectAttributes.addFlashAttribute("tipo", "success");
            return "redirect:/web/laboratorios/" + id;
        } catch (Exception e) {
            model.addAttribute("error", "Error al actualizar laboratorio: " + e.getMessage());
            model.addAttribute("laboratorioId", id);
            model.addAttribute("titulo", "Editar Laboratorio");
            model.addAttribute("accion", "Actualizar");
            return "laboratorios/formulario";
        }
    }

    @GetMapping("/{id}/eliminar")
    @PreAuthorize(PermissionConstants.LAB_WRITE)
    public String eliminarLaboratorio(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            laboratorioService.eliminar(id);
            redirectAttributes.addFlashAttribute("mensaje", "Laboratorio eliminado exitosamente");
            redirectAttributes.addFlashAttribute("tipo", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensaje", "Error al eliminar laboratorio: " + e.getMessage());
            redirectAttributes.addFlashAttribute("tipo", "danger");
        }
        return "redirect:/web/laboratorios";
    }

    @PostMapping("/{id}/eliminar")
    @PreAuthorize(PermissionConstants.LAB_WRITE)
    public String eliminarLaboratorioPost(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        return eliminarLaboratorio(id, redirectAttributes);
    }

    @PostMapping("/{id}/soft-delete")
    @PreAuthorize(PermissionConstants.LAB_WRITE)
    public String softDeleteLaboratorio(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            laboratorioService.softDelete(id);
            redirectAttributes.addFlashAttribute("mensaje", "Laboratorio enviado a papelera");
            redirectAttributes.addFlashAttribute("tipo", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensaje", "Error al eliminar laboratorio: " + e.getMessage());
            redirectAttributes.addFlashAttribute("tipo", "danger");
        }
        return "redirect:/web/laboratorios";
    }

    @PostMapping("/{id}/restore")
    @PreAuthorize(PermissionConstants.LAB_WRITE)
    public String restoreLaboratorio(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            laboratorioService.restore(id);
            redirectAttributes.addFlashAttribute("mensaje", "Laboratorio restaurado exitosamente");
            redirectAttributes.addFlashAttribute("tipo", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensaje", "Error al restaurar laboratorio: " + e.getMessage());
            redirectAttributes.addFlashAttribute("tipo", "danger");
        }
        return "redirect:/web/laboratorios/eliminados";
    }

    @PostMapping("/{id}/hard-delete")
    @PreAuthorize(PermissionConstants.LAB_WRITE)
    public String hardDeleteLaboratorio(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            laboratorioService.hardDelete(id);
            redirectAttributes.addFlashAttribute("mensaje", "Laboratorio eliminado permanentemente");
            redirectAttributes.addFlashAttribute("tipo", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensaje", "Error al eliminar definitivamente: " + e.getMessage());
            redirectAttributes.addFlashAttribute("tipo", "danger");
        }
        return "redirect:/web/laboratorios/eliminados";
    }
}

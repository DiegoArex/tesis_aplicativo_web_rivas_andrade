package com.example.demo.controller;

import com.example.demo.dto.EquipoRequestDTO;
import com.example.demo.dto.EquipoResponseDTO;
import com.example.demo.security.PermissionConstants;
import com.example.demo.service.EquipoService;
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
 * Controlador Web para gestión de Equipos con Thymeleaf
 */
@Controller
@RequestMapping("/web/equipos")
@RequiredArgsConstructor
public class WebEquipoController {

    private final EquipoService equipoService;
    private final LaboratorioService laboratorioService;

    @GetMapping
    @PreAuthorize(PermissionConstants.AUTHENTICATED)
    public String listarEquipos(Model model) {
        List<EquipoResponseDTO> equipos = equipoService.listarTodos();
        model.addAttribute("equipos", equipos);
        model.addAttribute("titulo", "Equipos");
        return "equipos/lista";
    }

    @GetMapping("/activos")
    @PreAuthorize(PermissionConstants.AUTHENTICATED)
    public String listarActivos(Model model) {
        List<EquipoResponseDTO> equipos = equipoService.listarActivos();
        model.addAttribute("equipos", equipos);
        model.addAttribute("titulo", "Equipos Activos");
        model.addAttribute("filtro", "activos");
        return "equipos/lista";
    }

    @GetMapping("/eliminados")
    @PreAuthorize(PermissionConstants.EQUIPO_WRITE)
    public String listarEliminados(Model model) {
        List<EquipoResponseDTO> equipos = equipoService.listarEliminados();
        model.addAttribute("equipos", equipos);
        model.addAttribute("titulo", "Equipos Eliminados");
        model.addAttribute("filtro", "eliminados");
        return "equipos/eliminados";
    }

    @GetMapping("/laboratorio/{laboratorioId}")
    @PreAuthorize(PermissionConstants.AUTHENTICATED)
    public String listarPorLaboratorio(@PathVariable Long laboratorioId, Model model) {
        List<EquipoResponseDTO> equipos = equipoService.listarPorLaboratorio(laboratorioId);
        model.addAttribute("equipos", equipos);
        model.addAttribute("titulo", "Equipos por Laboratorio");
        model.addAttribute("laboratorioId", laboratorioId);
        model.addAttribute("laboratorio", laboratorioService.obtenerPorId(laboratorioId));
        return "equipos/lista";
    }

    @GetMapping("/estado/{estado}")
    @PreAuthorize(PermissionConstants.AUTHENTICATED)
    public String listarPorEstado(@PathVariable String estado, Model model) {
        List<EquipoResponseDTO> equipos = equipoService.listarPorEstado(estado);
        model.addAttribute("equipos", equipos);
        model.addAttribute("titulo", "Equipos por Estado");
        model.addAttribute("estado", estado);
        return "equipos/lista";
    }

    @GetMapping("/{id}")
    @PreAuthorize(PermissionConstants.AUTHENTICATED)
    public String verEquipo(@PathVariable Long id, Model model) {
        EquipoResponseDTO equipo = equipoService.obtenerPorId(id);
        model.addAttribute("equipo", equipo);
        model.addAttribute("titulo", "Detalle de Equipo");
        return "equipos/detalle";
    }

    @GetMapping("/nuevo")
    @PreAuthorize(PermissionConstants.EQUIPO_WRITE)
    public String mostrarFormularioNuevo(Model model) {
        model.addAttribute("equipo", new EquipoRequestDTO());
        model.addAttribute("laboratorios", laboratorioService.listarTodos());
        model.addAttribute("titulo", "Nuevo Equipo");
        model.addAttribute("accion", "Crear");
        return "equipos/formulario";
    }

    @PostMapping
    @PreAuthorize(PermissionConstants.EQUIPO_WRITE)
    public String crearEquipo(@Valid @ModelAttribute("equipo") EquipoRequestDTO equipoDTO,
                             BindingResult result,
                             RedirectAttributes redirectAttributes,
                             Model model) {
        if (result.hasErrors()) {
            model.addAttribute("laboratorios", laboratorioService.listarTodos());
            model.addAttribute("titulo", "Nuevo Equipo");
            model.addAttribute("accion", "Crear");
            return "equipos/formulario";
        }

        try {
            EquipoResponseDTO creado = equipoService.crear(equipoDTO);
            redirectAttributes.addFlashAttribute("mensaje", "Equipo creado exitosamente");
            redirectAttributes.addFlashAttribute("tipo", "success");
            return "redirect:/web/equipos/" + creado.getId();
        } catch (Exception e) {
            model.addAttribute("error", "Error al crear equipo: " + e.getMessage());
            model.addAttribute("laboratorios", laboratorioService.listarTodos());
            model.addAttribute("titulo", "Nuevo Equipo");
            model.addAttribute("accion", "Crear");
            return "equipos/formulario";
        }
    }

    @GetMapping("/{id}/editar")
    @PreAuthorize(PermissionConstants.EQUIPO_WRITE)
    public String mostrarFormularioEditar(@PathVariable Long id, Model model) {
        EquipoResponseDTO equipo = equipoService.obtenerPorId(id);
        
        EquipoRequestDTO equipoRequest = new EquipoRequestDTO();
        equipoRequest.setCodigo(equipo.getCodigo());
        equipoRequest.setTipo(equipo.getTipo());
        equipoRequest.setNumeroSerie(equipo.getNumeroSerie());
        equipoRequest.setMarca(equipo.getMarca());
        equipoRequest.setModelo(equipo.getModelo());
        equipoRequest.setEstado(equipo.getEstado());
        equipoRequest.setLaboratorioId(equipo.getLaboratorioId());
        
        model.addAttribute("equipo", equipoRequest);
        model.addAttribute("equipoId", id);
        model.addAttribute("laboratorios", laboratorioService.listarTodos());
        model.addAttribute("titulo", "Editar Equipo");
        model.addAttribute("accion", "Actualizar");
        return "equipos/formulario";
    }

    @PostMapping("/{id}/editar")
    @PreAuthorize(PermissionConstants.EQUIPO_WRITE)
    public String actualizarEquipo(@PathVariable Long id,
                                  @Valid @ModelAttribute("equipo") EquipoRequestDTO equipoDTO,
                                  BindingResult result,
                                  RedirectAttributes redirectAttributes,
                                  Model model) {
        if (result.hasErrors()) {
            model.addAttribute("equipoId", id);
            model.addAttribute("laboratorios", laboratorioService.listarTodos());
            model.addAttribute("titulo", "Editar Equipo");
            model.addAttribute("accion", "Actualizar");
            return "equipos/formulario";
        }

        try {
            equipoService.actualizar(id, equipoDTO);
            redirectAttributes.addFlashAttribute("mensaje", "Equipo actualizado exitosamente");
            redirectAttributes.addFlashAttribute("tipo", "success");
            return "redirect:/web/equipos/" + id;
        } catch (Exception e) {
            model.addAttribute("error", "Error al actualizar equipo: " + e.getMessage());
            model.addAttribute("equipoId", id);
            model.addAttribute("laboratorios", laboratorioService.listarTodos());
            model.addAttribute("titulo", "Editar Equipo");
            model.addAttribute("accion", "Actualizar");
            return "equipos/formulario";
        }
    }

    @GetMapping("/{id}/eliminar")
    @PreAuthorize(PermissionConstants.EQUIPO_WRITE)
    public String eliminarEquipo(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            equipoService.eliminar(id);
            redirectAttributes.addFlashAttribute("mensaje", "Equipo eliminado exitosamente");
            redirectAttributes.addFlashAttribute("tipo", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensaje", "Error al eliminar equipo: " + e.getMessage());
            redirectAttributes.addFlashAttribute("tipo", "danger");
        }
        return "redirect:/web/equipos";
    }

    @PostMapping("/{id}/eliminar")
    @PreAuthorize(PermissionConstants.EQUIPO_WRITE)
    public String eliminarEquipoPost(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        return eliminarEquipo(id, redirectAttributes);
    }

    @PostMapping("/{id}/soft-delete")
    @PreAuthorize(PermissionConstants.EQUIPO_WRITE)
    public String softDeleteEquipo(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            equipoService.softDelete(id);
            redirectAttributes.addFlashAttribute("mensaje", "Equipo enviado a papelera");
            redirectAttributes.addFlashAttribute("tipo", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensaje", "Error al eliminar equipo: " + e.getMessage());
            redirectAttributes.addFlashAttribute("tipo", "danger");
        }
        return "redirect:/web/equipos";
    }

    @PostMapping("/{id}/restore")
    @PreAuthorize(PermissionConstants.EQUIPO_WRITE)
    public String restoreEquipo(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            equipoService.restore(id);
            redirectAttributes.addFlashAttribute("mensaje", "Equipo restaurado exitosamente");
            redirectAttributes.addFlashAttribute("tipo", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensaje", "Error al restaurar equipo: " + e.getMessage());
            redirectAttributes.addFlashAttribute("tipo", "danger");
        }
        return "redirect:/web/equipos/eliminados";
    }

    @PostMapping("/{id}/hard-delete")
    @PreAuthorize(PermissionConstants.EQUIPO_WRITE)
    public String hardDeleteEquipo(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            equipoService.hardDelete(id);
            redirectAttributes.addFlashAttribute("mensaje", "Equipo eliminado permanentemente");
            redirectAttributes.addFlashAttribute("tipo", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensaje", "Error al eliminar definitivamente: " + e.getMessage());
            redirectAttributes.addFlashAttribute("tipo", "danger");
        }
        return "redirect:/web/equipos/eliminados";
    }
}

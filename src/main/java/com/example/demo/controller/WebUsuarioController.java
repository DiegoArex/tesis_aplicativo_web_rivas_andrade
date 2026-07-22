package com.example.demo.controller;

import com.example.demo.annotation.CurrentUser;
import com.example.demo.dto.UsuarioRequestDTO;
import com.example.demo.dto.UsuarioResponseDTO;
import com.example.demo.security.PermissionConstants;
import com.example.demo.service.UsuarioService;
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
 * Controlador Web para gestión de Usuarios con Thymeleaf
 * Base URL: /web/usuarios
 */
@Controller
@RequestMapping("/web/usuarios")
@RequiredArgsConstructor
public class WebUsuarioController {

    private final UsuarioService usuarioService;

    // GET /web/usuarios - Listar todos los usuarios
    @GetMapping
    @PreAuthorize(PermissionConstants.USUARIO_ADMIN)
    public String listarUsuarios(Model model) {
        List<UsuarioResponseDTO> usuarios = usuarioService.listarTodos();
        model.addAttribute("usuarios", usuarios);
        model.addAttribute("titulo", "Gestión de Usuarios");
        return "usuarios/lista";
    }

    @GetMapping("/activos")
    @PreAuthorize(PermissionConstants.USUARIO_ADMIN)
    public String listarUsuariosActivos(Model model) {
        List<UsuarioResponseDTO> usuarios = usuarioService.listarActivos();
        model.addAttribute("usuarios", usuarios);
        model.addAttribute("titulo", "Usuarios Activos");
        model.addAttribute("filtro", "activos");
        return "usuarios/lista";
    }

    @GetMapping("/eliminados")
    @PreAuthorize(PermissionConstants.USUARIO_ADMIN)
    public String listarUsuariosEliminados(Model model) {
        List<UsuarioResponseDTO> usuarios = usuarioService.listarEliminados();
        model.addAttribute("usuarios", usuarios);
        model.addAttribute("titulo", "Usuarios Eliminados");
        model.addAttribute("filtro", "eliminados");
        return "usuarios/eliminados";
    }

    // GET /web/usuarios/{id} - Ver detalle de usuario
    @GetMapping("/{id}")
    @PreAuthorize(PermissionConstants.USUARIO_ADMIN)
    public String verUsuario(@PathVariable Long id, Model model) {
        UsuarioResponseDTO usuario = usuarioService.obtenerPorId(id);
        model.addAttribute("usuario", usuario);
        model.addAttribute("titulo", "Detalle de Usuario");
        return "usuarios/detalle";
    }

    // GET /web/usuarios/nuevo - Formulario para crear usuario
    @GetMapping("/nuevo")
    @PreAuthorize(PermissionConstants.USUARIO_ADMIN)
    public String mostrarFormularioNuevo(Model model) {
        model.addAttribute("usuario", new UsuarioRequestDTO());
        model.addAttribute("titulo", "Nuevo Usuario");
        model.addAttribute("accion", "Crear");
        return "usuarios/formulario";
    }

    // POST /web/usuarios - Crear nuevo usuario
    @PostMapping
    @PreAuthorize(PermissionConstants.USUARIO_ADMIN)
    public String crearUsuario(@Valid @ModelAttribute("usuario") UsuarioRequestDTO usuarioDTO,
                              BindingResult result,
                              RedirectAttributes redirectAttributes,
                              Model model) {
        if (result.hasErrors()) {
            model.addAttribute("titulo", "Nuevo Usuario");
            model.addAttribute("accion", "Crear");
            return "usuarios/formulario";
        }

        try {
            UsuarioResponseDTO creado = usuarioService.crear(usuarioDTO);
            redirectAttributes.addFlashAttribute("mensaje", "Usuario creado exitosamente");
            redirectAttributes.addFlashAttribute("tipo", "success");
            return "redirect:/web/usuarios/" + creado.getId();
        } catch (Exception e) {
            model.addAttribute("error", "Error al crear usuario: " + e.getMessage());
            model.addAttribute("titulo", "Nuevo Usuario");
            model.addAttribute("accion", "Crear");
            return "usuarios/formulario";
        }
    }

    // GET /web/usuarios/{id}/editar - Formulario para editar usuario
    @GetMapping("/{id}/editar")
    @PreAuthorize(PermissionConstants.USUARIO_ADMIN)
    public String mostrarFormularioEditar(@PathVariable Long id, Model model) {
        UsuarioResponseDTO usuario = usuarioService.obtenerPorId(id);
        
        // Convertir DTO de respuesta a DTO de request para el formulario
        UsuarioRequestDTO usuarioRequest = new UsuarioRequestDTO();
        usuarioRequest.setNombreCompleto(usuario.getNombreCompleto());
        usuarioRequest.setEmail(usuario.getEmail());
        usuarioRequest.setCarrera(usuario.getCarrera());
        usuarioRequest.setFacultad(usuario.getFacultad());
        
        model.addAttribute("usuario", usuarioRequest);
        model.addAttribute("usuarioId", id);
        model.addAttribute("titulo", "Editar Usuario");
        model.addAttribute("accion", "Actualizar");
        return "usuarios/formulario";
    }

    // POST /web/usuarios/{id}/editar - Actualizar usuario
    @PostMapping("/{id}/editar")
    @PreAuthorize(PermissionConstants.USUARIO_ADMIN)
    public String actualizarUsuario(@PathVariable Long id,
                                   @Valid @ModelAttribute("usuario") UsuarioRequestDTO usuarioDTO,
                                   BindingResult result,
                                   RedirectAttributes redirectAttributes,
                                   Model model) {
        if (result.hasErrors()) {
            model.addAttribute("usuarioId", id);
            model.addAttribute("titulo", "Editar Usuario");
            model.addAttribute("accion", "Actualizar");
            return "usuarios/formulario";
        }

        try {
            usuarioService.actualizar(id, usuarioDTO);
            redirectAttributes.addFlashAttribute("mensaje", "Usuario actualizado exitosamente");
            redirectAttributes.addFlashAttribute("tipo", "success");
            return "redirect:/web/usuarios/" + id;
        } catch (Exception e) {
            model.addAttribute("error", "Error al actualizar usuario: " + e.getMessage());
            model.addAttribute("usuarioId", id);
            model.addAttribute("titulo", "Editar Usuario");
            model.addAttribute("accion", "Actualizar");
            return "usuarios/formulario";
        }
    }

    // POST /web/usuarios/{id}/eliminar - Eliminar usuario (soft delete)
    @PostMapping("/{id}/eliminar")
    @PreAuthorize(PermissionConstants.USUARIO_ADMIN)
    public String eliminarUsuario(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            usuarioService.softDelete(id);
            redirectAttributes.addFlashAttribute("mensaje", "Usuario eliminado exitosamente");
            redirectAttributes.addFlashAttribute("tipo", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensaje", "Error al eliminar usuario: " + e.getMessage());
            redirectAttributes.addFlashAttribute("tipo", "error");
        }
        return "redirect:/web/usuarios";
    }

    @PostMapping("/{id}/desactivar")
    @PreAuthorize(PermissionConstants.USUARIO_ADMIN)
    public String desactivarUsuario(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            usuarioService.desactivar(id);
            redirectAttributes.addFlashAttribute("mensaje", "Usuario desactivado exitosamente");
            redirectAttributes.addFlashAttribute("tipo", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensaje", "Error al desactivar usuario: " + e.getMessage());
            redirectAttributes.addFlashAttribute("tipo", "error");
        }
        return "redirect:/web/usuarios/" + id;
    }

    @PostMapping("/{id}/restore")
    @PreAuthorize(PermissionConstants.USUARIO_ADMIN)
    public String restaurarUsuario(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            usuarioService.restore(id);
            redirectAttributes.addFlashAttribute("mensaje", "Usuario restaurado exitosamente");
            redirectAttributes.addFlashAttribute("tipo", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensaje", "Error al restaurar usuario: " + e.getMessage());
            redirectAttributes.addFlashAttribute("tipo", "danger");
        }
        return "redirect:/web/usuarios/eliminados";
    }

    @PostMapping("/{id}/hard-delete")
    @PreAuthorize(PermissionConstants.USUARIO_ADMIN)
    public String hardDeleteUsuario(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            usuarioService.hardDelete(id);
            redirectAttributes.addFlashAttribute("mensaje", "Usuario eliminado permanentemente");
            redirectAttributes.addFlashAttribute("tipo", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensaje", "Error al eliminar definitivamente: " + e.getMessage());
            redirectAttributes.addFlashAttribute("tipo", "danger");
        }
        return "redirect:/web/usuarios/eliminados";
    }

    // GET /web/usuarios/perfil - Ver perfil del usuario actual
    @GetMapping("/perfil")
    public String verPerfil(@CurrentUser String username, Model model) {
        UsuarioResponseDTO usuario = usuarioService.obtenerUsuarioActual(username);
        model.addAttribute("usuario", usuario);
        model.addAttribute("titulo", "Mi Perfil");
        return "usuarios/perfil";
    }
}

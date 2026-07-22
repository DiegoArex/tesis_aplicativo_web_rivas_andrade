package com.example.demo.controller;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.stream.Collectors;

import com.example.demo.annotation.CurrentUser;
import com.example.demo.dto.RegistroUsoEntradaDTO;
import com.example.demo.dto.RegistroUsoResponseDTO;
import com.example.demo.dto.RegistroUsoSalidaDTO;
import com.example.demo.security.PermissionConstants;
import com.example.demo.service.LaboratorioService;
import com.example.demo.service.RegistroUsoService;
import com.example.demo.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Controlador Web para gestión de Registros de Uso con Thymeleaf
 * Base URL: /web/registros
 */
@Controller
@RequestMapping("/web/registros")
@RequiredArgsConstructor
public class WebRegistroUsoController {

    private final RegistroUsoService registroUsoService;
    private final LaboratorioService laboratorioService;
    private final UsuarioService usuarioService;

    // GET /web/registros - Listar registros activos o de un usuario
    @GetMapping
    @PreAuthorize(PermissionConstants.DOCENTES)
    public String listarRegistros(Model model, Authentication authentication, @CurrentUser String username) {
        // Si es profesor común (no admin/director), mostrar solo sus registros
        boolean esAdmin = authentication != null && 
            authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || 
                              a.getAuthority().equals("ROLE_FACULTAD_DIRECTOR_CARRERA"));
        
        List<RegistroUsoResponseDTO> registros;
        if (esAdmin) {
            // Admin y directores ven todos los registros (abiertos y cerrados)
            List<RegistroUsoResponseDTO> todos = registroUsoService.obtenerTodosRegistros();
            List<RegistroUsoResponseDTO> activos = todos.stream()
                .filter(r -> r.getFechaSalida() == null)
                .sorted(Comparator.comparing(RegistroUsoResponseDTO::getFechaEntrada).reversed())
                .collect(Collectors.toList());
            List<RegistroUsoResponseDTO> cerrados = todos.stream()
                .filter(r -> r.getFechaSalida() != null)
                .sorted(Comparator.comparing(RegistroUsoResponseDTO::getFechaEntrada).reversed())
                .collect(Collectors.toList());
            registros = new ArrayList<>();
            registros.addAll(activos);
            registros.addAll(cerrados);
        } else {
            // Los profesores ven solo su historial completo
            List<RegistroUsoResponseDTO> todos = registroUsoService.obtenerMisRegistros(username);
            List<RegistroUsoResponseDTO> activos = todos.stream()
                .filter(r -> r.getFechaSalida() == null)
                .sorted(Comparator.comparing(RegistroUsoResponseDTO::getFechaEntrada).reversed())
                .collect(Collectors.toList());
            List<RegistroUsoResponseDTO> cerrados = todos.stream()
                .filter(r -> r.getFechaSalida() != null)
                .sorted(Comparator.comparing(RegistroUsoResponseDTO::getFechaEntrada).reversed())
                .collect(Collectors.toList());
            registros = new ArrayList<>();
            registros.addAll(activos);
            registros.addAll(cerrados);
        }
        
        model.addAttribute("registros", registros);
        model.addAttribute("titulo", "Registros de Uso");
        model.addAttribute("esAdmin", esAdmin);
        return "registros/lista";
    }

    // GET /web/registros/activos - Ver solo registros activos (entrada sin salida)
    @GetMapping("/activos")
    @PreAuthorize(PermissionConstants.DOCENTES)
    public String listarRegistrosActivos(Model model) {
        List<RegistroUsoResponseDTO> registros = registroUsoService.obtenerRegistrosActivos();
        // Ordenar de más reciente a más antiguo
        registros = registros.stream()
            .sorted(Comparator.comparing(RegistroUsoResponseDTO::getFechaEntrada).reversed())
            .collect(Collectors.toList());
        model.addAttribute("registros", registros);
        model.addAttribute("titulo", "Registros Activos");
        model.addAttribute("esAdmin", true);
        return "registros/lista";
    }

    @GetMapping("/entrada")
    @PreAuthorize(PermissionConstants.REGISTRO_ENTRADA_SALIDA)
    public String mostrarFormularioEntrada(Model model) {
        model.addAttribute("registro", new RegistroUsoEntradaDTO());
        model.addAttribute("laboratorios", laboratorioService.listarDisponiblesParaEntrada());
        model.addAttribute("titulo", "Registrar Entrada");
        return "registros/entrada";
    }

    @PostMapping("/entrada")
    @PreAuthorize(PermissionConstants.REGISTRO_ENTRADA_SALIDA)
    public String registrarEntrada(@Valid @ModelAttribute("registro") RegistroUsoEntradaDTO registroDTO,
                                   BindingResult result,
                                   RedirectAttributes redirectAttributes,
                                   Model model,
                                   @CurrentUser String username) {
        if (result.hasErrors()) {
            model.addAttribute("laboratorios", laboratorioService.listarDisponiblesParaEntrada());
            model.addAttribute("titulo", "Registrar Entrada");
            return "registros/entrada";
        }

        try {
            var usuario = usuarioService.obtenerUsuarioActual(username);
            registroUsoService.registrarEntrada(
                    usuario.getUsername(),
                    usuario.getKeycloakId(),
                    usuario.getEmail(),
                    usuario.getNombreCompleto(),
                    usuario.getRoles(),
                    registroDTO);
            redirectAttributes.addFlashAttribute("mensaje", "Entrada registrada exitosamente");
            redirectAttributes.addFlashAttribute("tipo", "success");
            return "redirect:/web/registros";
        } catch (Exception e) {
            model.addAttribute("error", "Error al registrar entrada: " + e.getMessage());
            model.addAttribute("laboratorios", laboratorioService.listarDisponiblesParaEntrada());
            model.addAttribute("titulo", "Registrar Entrada");
            return "registros/entrada";
        }
    }

    @GetMapping("/salida")
    @PreAuthorize(PermissionConstants.REGISTRO_ENTRADA_SALIDA)
    public String mostrarFormularioSalida(Model model, Authentication authentication) {
        String username = authentication.getName();
        List<RegistroUsoResponseDTO> misRegistros = registroUsoService.obtenerMisRegistros(username);
        RegistroUsoResponseDTO activo = misRegistros.stream()
                .filter(RegistroUsoResponseDTO::getActivo)
                .findFirst()
                .orElse(null);

        model.addAttribute("registroActivo", activo);
        model.addAttribute("salida", new RegistroUsoSalidaDTO());
        model.addAttribute("titulo", "Registrar Salida");
        return "registros/salida";
    }

    @PostMapping("/salida")
    @PreAuthorize(PermissionConstants.REGISTRO_ENTRADA_SALIDA)
    public String registrarSalida(@ModelAttribute("salida") RegistroUsoSalidaDTO salidaDTO,
                                  Authentication authentication,
                                  RedirectAttributes redirectAttributes) {
        try {
            registroUsoService.registrarSalida(authentication.getName(), salidaDTO);
            redirectAttributes.addFlashAttribute("mensaje", "Salida registrada exitosamente");
            redirectAttributes.addFlashAttribute("tipo", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensaje", "Error al registrar salida: " + e.getMessage());
            redirectAttributes.addFlashAttribute("tipo", "danger");
        }
        return "redirect:/web/registros";
    }

    @GetMapping("/usuario/{usuarioId}")
    @PreAuthorize(PermissionConstants.REGISTRO_ADMIN)
    public String listarPorUsuario(@PathVariable Long usuarioId, Model model) {
        List<RegistroUsoResponseDTO> registros = registroUsoService.obtenerRegistrosPorUsuario(usuarioId);
        // Ordenar de más reciente a más antiguo
        registros = registros.stream()
            .sorted(Comparator.comparing(RegistroUsoResponseDTO::getFechaEntrada).reversed())
            .collect(Collectors.toList());
        model.addAttribute("registros", registros);
        model.addAttribute("titulo", "Registros por Usuario");
        model.addAttribute("usuarioId", usuarioId);
        return "registros/lista";
    }

    @GetMapping("/laboratorio/{laboratorioId}")
    @PreAuthorize(PermissionConstants.REGISTRO_ADMIN)
    public String listarPorLaboratorio(@PathVariable Long laboratorioId, Model model) {
        List<RegistroUsoResponseDTO> registros = registroUsoService.obtenerRegistrosPorLaboratorio(laboratorioId);
        // Ordenar de más reciente a más antiguo
        registros = registros.stream()
            .sorted(Comparator.comparing(RegistroUsoResponseDTO::getFechaEntrada).reversed())
            .collect(Collectors.toList());
        model.addAttribute("registros", registros);
        model.addAttribute("titulo", "Registros por Laboratorio");
        model.addAttribute("laboratorioId", laboratorioId);
        model.addAttribute("laboratorio", laboratorioService.obtenerPorId(laboratorioId));
        return "registros/lista";
    }

    @GetMapping("/rango-fechas")
    @PreAuthorize(PermissionConstants.REGISTRO_ADMIN)
    public String listarPorRangoFechas(
            @RequestParam("inicio") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam("fin") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin,
            Model model) {
        List<RegistroUsoResponseDTO> registros = registroUsoService.obtenerRegistrosPorRangoFechas(inicio, fin);
        // Ordenar de más reciente a más antiguo
        registros = registros.stream()
            .sorted(Comparator.comparing(RegistroUsoResponseDTO::getFechaEntrada).reversed())
            .collect(Collectors.toList());
        model.addAttribute("registros", registros);
        model.addAttribute("titulo", "Registros por Rango de Fechas");
        model.addAttribute("inicio", inicio);
        model.addAttribute("fin", fin);
        return "registros/lista";
    }
}

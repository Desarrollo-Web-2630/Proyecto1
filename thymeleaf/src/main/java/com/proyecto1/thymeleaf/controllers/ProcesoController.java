package com.proyecto1.thymeleaf.controllers;

import com.proyecto1.thymeleaf.dto.ProcesoRequestDTO;
import com.proyecto1.thymeleaf.dto.ProcesoResponseDTO;
import com.proyecto1.thymeleaf.services.ProcesoService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/procesos")
public class ProcesoController {

    private final ProcesoService procesoService;

    public ProcesoController(ProcesoService procesoService) {
        this.procesoService = procesoService;
    }

    @GetMapping
    public String listarProcesos(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        Long empresaId = obtenerEmpresaId(session, redirectAttributes);
        if (empresaId == null) {
            return "redirect:/usuarios/login";
        }

        List<ProcesoResponseDTO> procesos = procesoService.listarPorEmpresa(empresaId);
        model.addAttribute("procesos", procesos);
        return "procesos/lista";
    }

    @GetMapping("/{id}")
    public String verProceso(@PathVariable Long id,
                            Model model,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {
        Long empresaId = obtenerEmpresaId(session, redirectAttributes);
        if (empresaId == null) {
            return "redirect:/usuarios/login";
        }
        try {
            model.addAttribute("proceso", procesoService.obtenerPorIdYEmpresa(id, empresaId));
            return "procesos/detalle";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
            return "redirect:/procesos";
        }
    }

    @GetMapping("/nuevo")
    public String mostrarFormularioCrear(Model model) {
        model.addAttribute("proceso", new ProcesoRequestDTO());
        return "procesos/formulario";
    }

    @PostMapping("/guardar")
    public String guardarProceso(@Valid @ModelAttribute("proceso") ProcesoRequestDTO proceso,
                                BindingResult resultado,
                                Model model,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        Long empresaId = obtenerEmpresaId(session, redirectAttributes);
        if (empresaId == null) {
            return "redirect:/usuarios/login";
        }
        if (resultado.hasErrors()) {
            return "procesos/formulario";
        }
        try {
            procesoService.crearProceso(proceso, empresaId);
            redirectAttributes.addFlashAttribute("mensajeExito", "Proceso creado con exito.");
        } catch (Exception e) {
            model.addAttribute("mensajeError", e.getMessage());
            return "procesos/formulario";
        }
        return "redirect:/procesos";
    }

    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable Long id,
                                        Model model,
                                        HttpSession session,
                                        RedirectAttributes redirectAttributes) {
        Long empresaId = obtenerEmpresaId(session, redirectAttributes);
        if (empresaId == null) {
            return "redirect:/usuarios/login";
        }
        try {
            ProcesoResponseDTO proceso = procesoService.obtenerPorIdYEmpresa(id, empresaId);
            ProcesoRequestDTO requestDTO = new ProcesoRequestDTO();
            requestDTO.setNombre(proceso.getNombre());
            requestDTO.setDescripcion(proceso.getDescripcion());
            requestDTO.setCategoria(proceso.getCategoria());
            model.addAttribute("proceso", requestDTO);
            return "procesos/formulario";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
            return "redirect:/procesos";
        }
    }

    @PostMapping("/actualizar/{id}")
    public String actualizarProceso(@PathVariable Long id,
                                    @Valid @ModelAttribute("proceso") ProcesoRequestDTO proceso,
                                    BindingResult resultado,
                                    Model model,
                                    HttpSession session,
                                    RedirectAttributes redirectAttributes) {
        Long empresaId = obtenerEmpresaId(session, redirectAttributes);
        if (empresaId == null) {
            return "redirect:/usuarios/login";
        }
        if (resultado.hasErrors()) {
            return "procesos/formulario";
        }
        try {
            procesoService.actualizarProceso(id, proceso, empresaId);
            redirectAttributes.addFlashAttribute("mensajeExito", "Proceso actualizado con exito.");
        } catch (Exception e) {
            model.addAttribute("mensajeError", e.getMessage());
            return "procesos/formulario";
        }
        return "redirect:/procesos";
    }

    @PostMapping("/publicar/{id}")
    public String publicarProceso(@PathVariable Long id,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        Long empresaId = obtenerEmpresaId(session, redirectAttributes);
        if (empresaId == null) {
            return "redirect:/usuarios/login";
        }
        try {
            procesoService.publicarProceso(id, empresaId);
            redirectAttributes.addFlashAttribute("mensajeExito", "Proceso publicado con exito.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
        }
        return "redirect:/procesos";
    }

    @PostMapping("/eliminar/{id}")
    public String eliminarProceso(@PathVariable Long id,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        Long empresaId = obtenerEmpresaId(session, redirectAttributes);
        if (empresaId == null) {
            return "redirect:/usuarios/login";
        }
        try {
            procesoService.eliminarProceso(id, empresaId);
            redirectAttributes.addFlashAttribute("mensajeExito", "Proceso eliminado correctamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
        }
        return "redirect:/procesos";
    }

    private Long obtenerEmpresaId(HttpSession session, RedirectAttributes redirectAttributes) {
        Object empresaId = session.getAttribute("empresaId");
        if (empresaId == null) {
            redirectAttributes.addFlashAttribute("mensajeError", "Debe iniciar sesion para acceder a su empresa.");
            return null;
        }
        return Long.valueOf(empresaId.toString());
    }
}

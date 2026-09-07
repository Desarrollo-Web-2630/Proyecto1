package com.proyecto1.thymeleaf.controllers;

import com.proyecto1.thymeleaf.dto.ProcesoDTO;
import com.proyecto1.thymeleaf.model.Proceso;
import com.proyecto1.thymeleaf.services.ProcesoService;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Controlador MVC de procesos (HU-04 crear, HU-05 editar, HU-06 eliminar,
 * HU-07 consultar).
 */
@Controller
@RequestMapping("/procesos")
public class ProcesoController {

    private final ProcesoService procesoService;
    private final ModelMapper modelMapper;

    public ProcesoController(ProcesoService procesoService, ModelMapper modelMapper) {
        this.procesoService = procesoService;
        this.modelMapper = modelMapper;
    }

    // ID de empresa simulado (reemplazar por el ID de la sesion cuando entre Spring Security)
    private final Long EMPRESA_ID_MOCK = 1L;

    // 1. Listar los procesos de la empresa
    @GetMapping
    public String listarProcesos(Model model) {
        List<Proceso> procesos = procesoService.listarPorEmpresa(EMPRESA_ID_MOCK);

        model.addAttribute("procesos", procesos);
        return "procesos/lista";
    }

    // 2. Ver el detalle de un proceso
    @GetMapping("/{id}")
    public String verProceso(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("proceso", procesoService.obtenerPorIdYEmpresa(id, EMPRESA_ID_MOCK));
            return "procesos/detalle";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
            return "redirect:/procesos";
        }
    }

    // 3. Crear un proceso: mostrar formulario
    @GetMapping("/nuevo")
    public String mostrarFormularioCrear(Model model) {
        model.addAttribute("proceso", new ProcesoDTO());
        return "procesos/formulario";
    }

    // 4. Crear
    @PostMapping("/guardar")
    public String guardarProceso(@Valid @ModelAttribute("proceso") ProcesoDTO proceso,
                                 BindingResult resultado,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        // Con errores de validacion se vuelve al formulario conservando lo escrito
        if (resultado.hasErrors()) {
            return "procesos/formulario";
        }
        try {
            procesoService.crearProceso(proceso, EMPRESA_ID_MOCK);
            redirectAttributes.addFlashAttribute("mensajeExito", "Proceso creado con éxito.");
        } catch (Exception e) {
            // Las reglas de negocio (nombre duplicado) se ven al guardar, no al validar
            model.addAttribute("mensajeError", e.getMessage());
            return "procesos/formulario";
        }
        return "redirect:/procesos";
    }

    // 5. Formulario para editar un proceso existente
    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable Long id,
                                          Model model,
                                          RedirectAttributes redirectAttributes) {
        try {
            Proceso proceso = procesoService.obtenerPorIdYEmpresa(id, EMPRESA_ID_MOCK);
            model.addAttribute("proceso", modelMapper.map(proceso, ProcesoDTO.class));
            return "procesos/formulario";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
            return "redirect:/procesos";
        }
    }

    // 6. Actualizar
    @PostMapping("/actualizar/{id}")
    public String actualizarProceso(@PathVariable Long id,
                                    @Valid @ModelAttribute("proceso") ProcesoDTO proceso,
                                    BindingResult resultado,
                                    Model model,
                                    RedirectAttributes redirectAttributes) {
        if (resultado.hasErrors()) {
            return "procesos/formulario";
        }
        try {
            procesoService.actualizarProceso(id, proceso, EMPRESA_ID_MOCK);
            redirectAttributes.addFlashAttribute("mensajeExito", "Proceso actualizado con éxito.");
        } catch (Exception e) {
            model.addAttribute("mensajeError", e.getMessage());
            return "procesos/formulario";
        }
        return "redirect:/procesos";
    }

    // 7. Pasar el proceso de borrador a publicado
    @PostMapping("/publicar/{id}")
    public String publicarProceso(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            procesoService.publicarProceso(id, EMPRESA_ID_MOCK);
            redirectAttributes.addFlashAttribute("mensajeExito", "Proceso publicado con éxito.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
        }
        return "redirect:/procesos";
    }

    // 8. Eliminar
    @PostMapping("/eliminar/{id}")
    public String eliminarProceso(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            procesoService.eliminarProceso(id, EMPRESA_ID_MOCK);
            redirectAttributes.addFlashAttribute("mensajeExito", "Proceso eliminado correctamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
        }
        return "redirect:/procesos";
    }
}

package com.proyecto1.thymeleaf.controllers;

import com.proyecto1.thymeleaf.dto.ActividadDTO;
import com.proyecto1.thymeleaf.model.Actividad;
import com.proyecto1.thymeleaf.services.ActividadService;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Controlador MVC de actividades (HU-08 crear, HU-09 editar, HU-10 eliminar).
 *
 * Las rutas cuelgan del proceso porque una actividad no existe fuera de el.
 */
@Controller
@RequestMapping("/procesos/{procesoId}/actividades")
public class ActividadController {

    private final ActividadService actividadService;
    private final ModelMapper modelMapper;

    public ActividadController(ActividadService actividadService, ModelMapper modelMapper) {
        this.actividadService = actividadService;
        this.modelMapper = modelMapper;
    }

    // ID de empresa simulado (reemplazar por el ID de la sesion cuando entre Spring Security)
    private final Long EMPRESA_ID_MOCK = 1L;

    // 1. Listar las actividades de un proceso
    @GetMapping
    public String listarActividades(@PathVariable Long procesoId,
                                    Model model,
                                    RedirectAttributes redirectAttributes) {
        try {
            List<Actividad> actividades = actividadService.listarPorProcesoYEmpresa(procesoId, EMPRESA_ID_MOCK);
            model.addAttribute("actividades", actividades);
            model.addAttribute("procesoId", procesoId);
            return "actividades/lista";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
            return "redirect:/procesos";
        }
    }

    // 2. Crear una actividad: mostrar formulario
    @GetMapping("/nueva")
    public String mostrarFormularioCrear(@PathVariable Long procesoId, Model model) {
        model.addAttribute("actividad", new ActividadDTO());
        model.addAttribute("procesoId", procesoId);
        return "actividades/formulario";
    }

    // 3. Crear
    @PostMapping("/guardar")
    public String guardarActividad(@PathVariable Long procesoId,
                                   @Valid @ModelAttribute("actividad") ActividadDTO actividad,
                                   BindingResult resultado,
                                   Model model,
                                   RedirectAttributes redirectAttributes) {
        model.addAttribute("procesoId", procesoId);
        if (resultado.hasErrors()) {
            return "actividades/formulario";
        }
        try {
            actividadService.crearActividad(actividad, procesoId, EMPRESA_ID_MOCK);
            redirectAttributes.addFlashAttribute("mensajeExito", "Actividad creada con éxito.");
        } catch (Exception e) {
            model.addAttribute("mensajeError", e.getMessage());
            return "actividades/formulario";
        }
        return "redirect:/procesos/" + procesoId + "/actividades";
    }

    // 4. Formulario para editar una actividad existente
    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable Long procesoId,
                                          @PathVariable Long id,
                                          Model model,
                                          RedirectAttributes redirectAttributes) {
        try {
            Actividad actividad = actividadService.obtenerPorIdYEmpresa(id, EMPRESA_ID_MOCK);
            model.addAttribute("actividad", modelMapper.map(actividad, ActividadDTO.class));
            model.addAttribute("procesoId", procesoId);
            return "actividades/formulario";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
            return "redirect:/procesos/" + procesoId + "/actividades";
        }
    }

    // 5. Actualizar. Cambiar la lane cambia el responsable de la actividad (HU-09)
    @PostMapping("/actualizar/{id}")
    public String actualizarActividad(@PathVariable Long procesoId,
                                      @PathVariable Long id,
                                      @Valid @ModelAttribute("actividad") ActividadDTO actividad,
                                      BindingResult resultado,
                                      Model model,
                                      RedirectAttributes redirectAttributes) {
        model.addAttribute("procesoId", procesoId);
        if (resultado.hasErrors()) {
            return "actividades/formulario";
        }
        try {
            actividadService.actualizarActividad(id, actividad, EMPRESA_ID_MOCK);
            redirectAttributes.addFlashAttribute("mensajeExito", "Actividad actualizada con éxito.");
        } catch (Exception e) {
            model.addAttribute("mensajeError", e.getMessage());
            return "actividades/formulario";
        }
        return "redirect:/procesos/" + procesoId + "/actividades";
    }

    // 6. Mover la actividad en el diagrama sin tocar el resto de sus datos
    @PostMapping("/mover/{id}")
    public String moverActividad(@PathVariable Long procesoId,
                                 @PathVariable Long id,
                                 @RequestParam Integer posicionX,
                                 @RequestParam Integer posicionY,
                                 RedirectAttributes redirectAttributes) {
        try {
            actividadService.moverActividad(id, posicionX, posicionY, EMPRESA_ID_MOCK);
            redirectAttributes.addFlashAttribute("mensajeExito", "Actividad movida con éxito.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
        }
        return "redirect:/procesos/" + procesoId + "/actividades";
    }

    // 7. Eliminar (HU-10). El borrado es logico y la vista pide confirmacion.
    @PostMapping("/eliminar/{id}")
    public String eliminarActividad(@PathVariable Long procesoId,
                                    @PathVariable Long id,
                                    RedirectAttributes redirectAttributes) {
        try {
            actividadService.eliminarActividad(id, EMPRESA_ID_MOCK);
            redirectAttributes.addFlashAttribute("mensajeExito", "Actividad eliminada correctamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
        }
        return "redirect:/procesos/" + procesoId + "/actividades";
    }
}

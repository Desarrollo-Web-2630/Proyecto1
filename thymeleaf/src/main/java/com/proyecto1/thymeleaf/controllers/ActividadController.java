package com.proyecto1.thymeleaf.controllers;

import com.proyecto1.thymeleaf.dto.ActividadRequestDTO;
import com.proyecto1.thymeleaf.dto.ActividadResponseDTO;
import com.proyecto1.thymeleaf.services.ActividadService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/procesos/{procesoId}/actividades")
public class ActividadController {

    private final ActividadService actividadService;

    public ActividadController(ActividadService actividadService) {
        this.actividadService = actividadService;
    }

    private final Long EMPRESA_ID_MOCK = 1L;

    @GetMapping
    public String listarActividades(@PathVariable Long procesoId,
                                    Model model,
                                    RedirectAttributes redirectAttributes) {
        try {
            List<ActividadResponseDTO> actividades = actividadService.listarPorProcesoYEmpresa(procesoId, EMPRESA_ID_MOCK);
            model.addAttribute("actividades", actividades);
            model.addAttribute("procesoId", procesoId);
            return "actividades/lista";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
            return "redirect:/procesos";
        }
    }

    @GetMapping("/nueva")
    public String mostrarFormularioCrear(@PathVariable Long procesoId, Model model) {
        model.addAttribute("actividad", new ActividadRequestDTO());
        model.addAttribute("procesoId", procesoId);
        return "actividades/formulario";
    }

    @PostMapping("/guardar")
    public String guardarActividad(@PathVariable Long procesoId,
                                   @Valid @ModelAttribute("actividad") ActividadRequestDTO actividad,
                                   BindingResult resultado,
                                   Model model,
                                   RedirectAttributes redirectAttributes) {
        model.addAttribute("procesoId", procesoId);
        if (resultado.hasErrors()) {
            return "actividades/formulario";
        }
        try {
            actividadService.crearActividad(actividad, procesoId, EMPRESA_ID_MOCK);
            redirectAttributes.addFlashAttribute("mensajeExito", "Actividad creada con exito.");
        } catch (Exception e) {
            model.addAttribute("mensajeError", e.getMessage());
            return "actividades/formulario";
        }
        return "redirect:/procesos/" + procesoId + "/actividades";
    }

    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable Long procesoId,
                                          @PathVariable Long id,
                                          Model model,
                                          RedirectAttributes redirectAttributes) {
        try {
            ActividadResponseDTO actividad = actividadService.obtenerPorIdYEmpresa(id, EMPRESA_ID_MOCK);
            ActividadRequestDTO requestDTO = new ActividadRequestDTO();
            requestDTO.setNombre(actividad.getNombre());
            requestDTO.setTipoActividad(actividad.getTipoActividad());
            requestDTO.setPosicionX(actividad.getPosicionX());
            requestDTO.setPosicionY(actividad.getPosicionY());
            requestDTO.setLaneId(actividad.getLaneId());
            model.addAttribute("actividad", requestDTO);
            model.addAttribute("procesoId", procesoId);
            return "actividades/formulario";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
            return "redirect:/procesos/" + procesoId + "/actividades";
        }
    }

    @PostMapping("/actualizar/{id}")
    public String actualizarActividad(@PathVariable Long procesoId,
                                      @PathVariable Long id,
                                      @Valid @ModelAttribute("actividad") ActividadRequestDTO actividad,
                                      BindingResult resultado,
                                      Model model,
                                      RedirectAttributes redirectAttributes) {
        model.addAttribute("procesoId", procesoId);
        if (resultado.hasErrors()) {
            return "actividades/formulario";
        }
        try {
            actividadService.actualizarActividad(id, actividad, EMPRESA_ID_MOCK);
            redirectAttributes.addFlashAttribute("mensajeExito", "Actividad actualizada con exito.");
        } catch (Exception e) {
            model.addAttribute("mensajeError", e.getMessage());
            return "actividades/formulario";
        }
        return "redirect:/procesos/" + procesoId + "/actividades";
    }

    @PostMapping("/mover/{id}")
    public String moverActividad(@PathVariable Long procesoId,
                                 @PathVariable Long id,
                                 @RequestParam Integer posicionX,
                                 @RequestParam Integer posicionY,
                                 RedirectAttributes redirectAttributes) {
        try {
            actividadService.moverActividad(id, posicionX, posicionY, EMPRESA_ID_MOCK);
            redirectAttributes.addFlashAttribute("mensajeExito", "Actividad movida con exito.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
        }
        return "redirect:/procesos/" + procesoId + "/actividades";
    }

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

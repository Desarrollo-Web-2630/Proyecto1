package com.proyecto1.thymeleaf.controllers;

import com.proyecto1.thymeleaf.dto.ArcoDTO;
import com.proyecto1.thymeleaf.model.Arco;
import com.proyecto1.thymeleaf.services.ArcoService;
import com.proyecto1.thymeleaf.services.ElementoConectableService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Controlador MVC de arcos (HU-11 crear, HU-12 editar, HU-13 eliminar).
 *
 * El formulario necesita la lista de elementos del proceso para poder elegir
 * el origen y el destino, tanto al crear como al editar: la HU-12 permite
 * cambiar los dos extremos.
 */
@Controller
@RequestMapping("/procesos/{procesoId}/arcos")
public class ArcoController {

    private final ArcoService arcoService;
    private final ElementoConectableService elementoConectableService;

    public ArcoController(ArcoService arcoService, ElementoConectableService elementoConectableService) {
        this.arcoService = arcoService;
        this.elementoConectableService = elementoConectableService;
    }

    // ID de empresa simulado (reemplazar por el ID de la sesion cuando entre Spring Security)
    private final Long EMPRESA_ID_MOCK = 1L;

    // 1. Listar los arcos de un proceso, avisando cuales dejarian el diagrama roto
    @GetMapping
    public String listarArcos(@PathVariable Long procesoId,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        try {
            List<Arco> arcos = arcoService.listarPorProcesoYEmpresa(procesoId, EMPRESA_ID_MOCK);
            model.addAttribute("arcos", arcos);
            model.addAttribute("procesoId", procesoId);
            return "arcos/lista";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
            return "redirect:/procesos";
        }
    }

    // 2. Crear un arco: mostrar formulario
    @GetMapping("/nuevo")
    public String mostrarFormularioCrear(@PathVariable Long procesoId,
                                         Model model,
                                         RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("arco", new ArcoDTO());
            cargarElementos(model, procesoId);
            return "arcos/formulario";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
            return "redirect:/procesos/" + procesoId + "/arcos";
        }
    }

    // 3. Crear
    @PostMapping("/guardar")
    public String guardarArco(@PathVariable Long procesoId,
                              @Valid @ModelAttribute("arco") ArcoDTO arco,
                              BindingResult resultado,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        cargarElementos(model, procesoId);
        if (resultado.hasErrors()) {
            return "arcos/formulario";
        }
        try {
            arcoService.crearArco(arco, procesoId, EMPRESA_ID_MOCK);
            redirectAttributes.addFlashAttribute("mensajeExito", "Arco creado con éxito.");
        } catch (Exception e) {
            model.addAttribute("mensajeError", e.getMessage());
            return "arcos/formulario";
        }
        return "redirect:/procesos/" + procesoId + "/arcos";
    }

    // 4. Formulario para editar un arco existente
    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable Long procesoId,
                                          @PathVariable Long id,
                                          Model model,
                                          RedirectAttributes redirectAttributes) {
        try {
            Arco arco = arcoService.obtenerPorIdYEmpresa(id, EMPRESA_ID_MOCK);
            model.addAttribute("arco", new ArcoDTO(
                    arco.getId(),
                    arco.getNombre(),
                    arco.getCondicion(),
                    arco.getOrigen().getId(),
                    arco.getDestino().getId()));
            cargarElementos(model, procesoId);
            return "arcos/formulario";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
            return "redirect:/procesos/" + procesoId + "/arcos";
        }
    }

    // 5. Actualizar: la HU-12 permite cambiar tambien origen y destino
    @PostMapping("/actualizar/{id}")
    public String actualizarArco(@PathVariable Long procesoId,
                                 @PathVariable Long id,
                                 @Valid @ModelAttribute("arco") ArcoDTO arco,
                                 BindingResult resultado,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        cargarElementos(model, procesoId);
        if (resultado.hasErrors()) {
            return "arcos/formulario";
        }
        try {
            arcoService.actualizarArco(id, arco, EMPRESA_ID_MOCK);
            redirectAttributes.addFlashAttribute("mensajeExito", "Arco actualizado con éxito.");
        } catch (Exception e) {
            model.addAttribute("mensajeError", e.getMessage());
            return "arcos/formulario";
        }
        return "redirect:/procesos/" + procesoId + "/arcos";
    }

    // 6. Eliminar (HU-13). Antes de borrar se avisa si el diagrama queda roto.
    @PostMapping("/eliminar/{id}")
    public String eliminarArco(@PathVariable Long procesoId,
                               @PathVariable Long id,
                               RedirectAttributes redirectAttributes) {
        try {
            String advertencia = arcoService.advertenciaAlEliminar(id, EMPRESA_ID_MOCK);
            arcoService.eliminarArco(id, EMPRESA_ID_MOCK);

            redirectAttributes.addFlashAttribute("mensajeExito", "Arco eliminado correctamente.");
            if (advertencia != null) {
                redirectAttributes.addFlashAttribute("mensajeAdvertencia", advertencia);
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
        }
        return "redirect:/procesos/" + procesoId + "/arcos";
    }

    private void cargarElementos(Model model, Long procesoId) {
        model.addAttribute("elementos",
                elementoConectableService.listarElementosPorProcesoYEmpresa(procesoId, EMPRESA_ID_MOCK));
        model.addAttribute("procesoId", procesoId);
    }
}

package com.proyecto1.thymeleaf.controllers;

import com.proyecto1.thymeleaf.model.Arco;
import com.proyecto1.thymeleaf.services.ArcoService;
import com.proyecto1.thymeleaf.services.ElementoConectableService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Controlador MVC de arcos (HU-11 crear, HU-12 editar, HU-13 eliminar).
 *
 * El formulario necesita la lista de elementos del proceso para poder elegir
 * el origen y el destino del arco.
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

    // 1. Listar los arcos de un proceso
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
            model.addAttribute("arco", new Arco());
            model.addAttribute("elementos",
                    elementoConectableService.listarElementosPorProcesoYEmpresa(procesoId, EMPRESA_ID_MOCK));
            model.addAttribute("procesoId", procesoId);
            return "arcos/formulario";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
            return "redirect:/procesos/" + procesoId + "/arcos";
        }
    }

    // 3. Crear
    @PostMapping("/guardar")
    public String guardarArco(@PathVariable Long procesoId,
                              @ModelAttribute("arco") Arco arco,
                              @RequestParam Long origenId,
                              @RequestParam Long destinoId,
                              RedirectAttributes redirectAttributes) {
        try {
            arcoService.crearArco(arco, procesoId, origenId, destinoId, EMPRESA_ID_MOCK);
            redirectAttributes.addFlashAttribute("mensajeExito", "Arco creado con éxito.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
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
            model.addAttribute("arco", arcoService.obtenerPorIdYEmpresa(id, EMPRESA_ID_MOCK));
            model.addAttribute("elementos",
                    elementoConectableService.listarElementosPorProcesoYEmpresa(procesoId, EMPRESA_ID_MOCK));
            model.addAttribute("procesoId", procesoId);
            return "arcos/formulario";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
            return "redirect:/procesos/" + procesoId + "/arcos";
        }
    }

    // 5. Actualizar
    @PostMapping("/actualizar/{id}")
    public String actualizarArco(@PathVariable Long procesoId,
                                 @PathVariable Long id,
                                 @ModelAttribute("arco") Arco arco,
                                 RedirectAttributes redirectAttributes) {
        try {
            arcoService.actualizarArco(id, arco, EMPRESA_ID_MOCK);
            redirectAttributes.addFlashAttribute("mensajeExito", "Arco actualizado con éxito.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
        }
        return "redirect:/procesos/" + procesoId + "/arcos";
    }

    // 6. Eliminar
    @PostMapping("/eliminar/{id}")
    public String eliminarArco(@PathVariable Long procesoId,
                               @PathVariable Long id,
                               RedirectAttributes redirectAttributes) {
        try {
            arcoService.eliminarArco(id, EMPRESA_ID_MOCK);
            redirectAttributes.addFlashAttribute("mensajeExito", "Arco eliminado correctamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
        }
        return "redirect:/procesos/" + procesoId + "/arcos";
    }
}

package com.proyecto1.thymeleaf.controller;

import com.proyecto1.thymeleaf.dto.ArcoForm;
import com.proyecto1.thymeleaf.services.ArcoService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/procesos/{procesoId}/arcos")
public class ArcoController {

    private final ArcoService arcoService;

    public ArcoController(ArcoService arcoService) {
        this.arcoService = arcoService;
    }

    @GetMapping
    public String listar(@PathVariable Long procesoId,
                         @RequestParam Long empresaId,
                         Model model) {
        model.addAttribute("arcos", arcoService.listarPorProcesoYEmpresa(procesoId, empresaId));
        model.addAttribute("procesoId", procesoId);
        model.addAttribute("empresaId", empresaId);
        return "arcos/lista";
    }

    @GetMapping("/nuevo")
    public String formularioCrear(@PathVariable Long procesoId,
                                  @RequestParam Long empresaId,
                                  Model model) {
        prepararFormulario(model, procesoId, empresaId);
        model.addAttribute("arcoForm", new ArcoForm());
        return "arcos/formulario";
    }

    @PostMapping
    public String crear(@PathVariable Long procesoId,
                        @RequestParam Long empresaId,
                        @Valid @ModelAttribute("arcoForm") ArcoForm arcoForm,
                        BindingResult result,
                        Model model,
                        RedirectAttributes flash) {
        if (result.hasErrors()) {
            prepararFormulario(model, procesoId, empresaId);
            return "arcos/formulario";
        }
        try {
            arcoService.crearArco(arcoForm, procesoId, empresaId);
        } catch (IllegalArgumentException e) {
            result.reject("arco.invalido", e.getMessage());
            prepararFormulario(model, procesoId, empresaId);
            return "arcos/formulario";
        }
        flash.addFlashAttribute("exito", "Arco creado correctamente");
        return "redirect:/procesos/" + procesoId + "/arcos?empresaId=" + empresaId;
    }

    @GetMapping("/{id}/editar")
    public String formularioEditar(@PathVariable Long procesoId,
                                   @PathVariable Long id,
                                   @RequestParam Long empresaId,
                                   Model model) {
        prepararFormulario(model, procesoId, empresaId);
        model.addAttribute("arcoForm", ArcoForm.desdeEntidad(arcoService.obtenerPorIdYEmpresa(id, empresaId)));
        return "arcos/formulario";
    }

    @PostMapping("/{id}")
    public String actualizar(@PathVariable Long procesoId,
                             @PathVariable Long id,
                             @RequestParam Long empresaId,
                             @Valid @ModelAttribute("arcoForm") ArcoForm arcoForm,
                             BindingResult result,
                             Model model,
                             RedirectAttributes flash) {
        if (result.hasErrors()) {
            prepararFormulario(model, procesoId, empresaId);
            return "arcos/formulario";
        }
        try {
            arcoService.actualizarArco(id, arcoForm, empresaId);
        } catch (IllegalArgumentException e) {
            result.reject("arco.invalido", e.getMessage());
            prepararFormulario(model, procesoId, empresaId);
            return "arcos/formulario";
        }
        flash.addFlashAttribute("exito", "Arco actualizado correctamente");
        return "redirect:/procesos/" + procesoId + "/arcos?empresaId=" + empresaId;
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Long procesoId,
                           @PathVariable Long id,
                           @RequestParam Long empresaId,
                           RedirectAttributes flash) {
        try {
            List<String> advertencias = arcoService.eliminarArco(id, empresaId);
            flash.addFlashAttribute("exito", "Arco eliminado correctamente");
            if (!advertencias.isEmpty()) {
                flash.addFlashAttribute("advertencias", advertencias);
            }
        } catch (IllegalArgumentException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/procesos/" + procesoId + "/arcos?empresaId=" + empresaId;
    }

    private void prepararFormulario(Model model, Long procesoId, Long empresaId) {
        model.addAttribute("elementos", arcoService.listarElementosConectables(procesoId, empresaId));
        model.addAttribute("procesoId", procesoId);
        model.addAttribute("empresaId", empresaId);
    }
}

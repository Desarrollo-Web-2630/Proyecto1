package com.proyecto1.thymeleaf.controllers;

import com.proyecto1.thymeleaf.dto.EmpresaDTO;
import com.proyecto1.thymeleaf.model.Empresa;
import com.proyecto1.thymeleaf.services.EmpresaService;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Controlador MVC de empresas (HU-01 Registro de empresa).
 *
 * No lleva empresaId simulado: la empresa es la raiz del tenant, no algo que
 * se consulte dentro de uno.
 */
@Controller
@RequestMapping("/empresas")
public class EmpresaController {

    private final EmpresaService empresaService;
    private final ModelMapper modelMapper;

    public EmpresaController(EmpresaService empresaService, ModelMapper modelMapper) {
        this.empresaService = empresaService;
        this.modelMapper = modelMapper;
    }

    // 1. Listar las empresas registradas
    @GetMapping
    public String listarEmpresas(Model model) {
        List<Empresa> empresas = empresaService.listarTodas();

        model.addAttribute("empresas", empresas);
        return "empresas/lista";
    }

    // 2. Ver el detalle de una empresa
    @GetMapping("/{id}")
    public String verEmpresa(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("empresa", empresaService.obtenerPorId(id));
            return "empresas/detalle";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
            return "redirect:/empresas";
        }
    }

    // 3. Registrar una empresa: mostrar formulario
    @GetMapping("/nueva")
    public String mostrarFormularioRegistro(Model model) {
        model.addAttribute("empresa", new EmpresaDTO());
        return "empresas/formulario";
    }

    // 4. Registrar
    @PostMapping("/guardar")
    public String guardarEmpresa(@Valid @ModelAttribute("empresa") EmpresaDTO empresa,
                                 BindingResult resultado,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        if (resultado.hasErrors()) {
            return "empresas/formulario";
        }
        try {
            empresaService.registrarEmpresa(empresa);
            redirectAttributes.addFlashAttribute("mensajeExito", "Empresa registrada con éxito.");
        } catch (Exception e) {
            model.addAttribute("mensajeError", e.getMessage());
            return "empresas/formulario";
        }
        return "redirect:/empresas";
    }

    // 5. Formulario para editar una empresa existente
    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable Long id,
                                          Model model,
                                          RedirectAttributes redirectAttributes) {
        try {
            Empresa empresa = empresaService.obtenerPorId(id);
            model.addAttribute("empresa", modelMapper.map(empresa, EmpresaDTO.class));
            return "empresas/formulario";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
            return "redirect:/empresas";
        }
    }

    // 6. Actualizar
    @PostMapping("/actualizar/{id}")
    public String actualizarEmpresa(@PathVariable Long id,
                                    @Valid @ModelAttribute("empresa") EmpresaDTO empresa,
                                    BindingResult resultado,
                                    Model model,
                                    RedirectAttributes redirectAttributes) {
        if (resultado.hasErrors()) {
            return "empresas/formulario";
        }
        try {
            empresaService.actualizarEmpresa(id, empresa);
            redirectAttributes.addFlashAttribute("mensajeExito", "Empresa actualizada con éxito.");
        } catch (Exception e) {
            model.addAttribute("mensajeError", e.getMessage());
            return "empresas/formulario";
        }
        return "redirect:/empresas";
    }

    // 7. Eliminar
    @PostMapping("/eliminar/{id}")
    public String eliminarEmpresa(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            empresaService.eliminarEmpresa(id);
            redirectAttributes.addFlashAttribute("mensajeExito", "Empresa eliminada correctamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
        }
        return "redirect:/empresas";
    }
}

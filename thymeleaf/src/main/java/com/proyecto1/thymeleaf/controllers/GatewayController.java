package com.proyecto1.thymeleaf.controllers;

import com.proyecto1.thymeleaf.dto.GatewayDTO;
import com.proyecto1.thymeleaf.model.Gateway;
import com.proyecto1.thymeleaf.services.GatewayService;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Controlador MVC de gateways (HU-14 crear, HU-15 editar, HU-16 eliminar).
 */
@Controller
@RequestMapping("/procesos/{procesoId}/gateways")
public class GatewayController {

    private final GatewayService gatewayService;
    private final ModelMapper modelMapper;

    public GatewayController(GatewayService gatewayService, ModelMapper modelMapper) {
        this.gatewayService = gatewayService;
        this.modelMapper = modelMapper;
    }

    // ID de empresa simulado (reemplazar por el ID de la sesion cuando entre Spring Security)
    private final Long EMPRESA_ID_MOCK = 1L;

    // 1. Vista para listar todos los gateways de un proceso
    @GetMapping
    public String listarGateways(@PathVariable Long procesoId,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        try {
            List<Gateway> gateways = gatewayService.listarPorProcesoYEmpresa(procesoId, EMPRESA_ID_MOCK);
            model.addAttribute("gateways", gateways);
            model.addAttribute("procesoId", procesoId);
            return "gateways/lista";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
            return "redirect:/procesos";
        }
    }

    // 2. Crear un nuevo gateway: mostrar formulario
    @GetMapping("/nuevo")
    public String mostrarFormularioCrear(@PathVariable Long procesoId, Model model) {
        model.addAttribute("gateway", new GatewayDTO());
        cargarOpciones(model, procesoId);
        return "gateways/formulario";
    }

    // 3. Crear
    @PostMapping("/guardar")
    public String guardarGateway(@PathVariable Long procesoId,
                                 @Valid @ModelAttribute("gateway") GatewayDTO gateway,
                                 BindingResult resultado,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        cargarOpciones(model, procesoId);
        if (resultado.hasErrors()) {
            return "gateways/formulario";
        }
        try {
            gatewayService.crearGateway(gateway, procesoId, EMPRESA_ID_MOCK);
            redirectAttributes.addFlashAttribute("mensajeExito", "Compuerta creada con éxito.");
        } catch (Exception e) {
            model.addAttribute("mensajeError", e.getMessage());
            return "gateways/formulario";
        }
        return "redirect:/procesos/" + procesoId + "/gateways";
    }

    // 4. Formulario para editar un gateway existente
    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable Long procesoId,
                                          @PathVariable Long id,
                                          Model model,
                                          RedirectAttributes redirectAttributes) {
        try {
            Gateway gateway = gatewayService.obtenerPorIdYEmpresa(id, EMPRESA_ID_MOCK);
            model.addAttribute("gateway", modelMapper.map(gateway, GatewayDTO.class));
            cargarOpciones(model, procesoId);
            return "gateways/formulario";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
            return "redirect:/procesos/" + procesoId + "/gateways";
        }
    }

    // 5. Actualizar
    @PostMapping("/actualizar/{id}")
    public String actualizarGateway(@PathVariable Long procesoId,
                                    @PathVariable Long id,
                                    @Valid @ModelAttribute("gateway") GatewayDTO gateway,
                                    BindingResult resultado,
                                    Model model,
                                    RedirectAttributes redirectAttributes) {
        cargarOpciones(model, procesoId);
        if (resultado.hasErrors()) {
            return "gateways/formulario";
        }
        try {
            gatewayService.actualizarGateway(id, gateway, EMPRESA_ID_MOCK);
            redirectAttributes.addFlashAttribute("mensajeExito", "Compuerta actualizada con éxito.");
        } catch (Exception e) {
            model.addAttribute("mensajeError", e.getMessage());
            return "gateways/formulario";
        }
        return "redirect:/procesos/" + procesoId + "/gateways";
    }

    // 6. Eliminar (soft delete via @SQLDelete)
    @PostMapping("/eliminar/{id}")
    public String eliminarGateway(@PathVariable Long procesoId,
                                  @PathVariable Long id,
                                  RedirectAttributes redirectAttributes) {
        try {
            gatewayService.eliminarGateway(id, EMPRESA_ID_MOCK);
            redirectAttributes.addFlashAttribute("mensajeExito", "Compuerta eliminada correctamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
        }
        return "redirect:/procesos/" + procesoId + "/gateways";
    }

    private void cargarOpciones(Model model, Long procesoId) {
        model.addAttribute("tiposGateway", Gateway.TipoGateway.values());
        model.addAttribute("procesoId", procesoId);
    }
}

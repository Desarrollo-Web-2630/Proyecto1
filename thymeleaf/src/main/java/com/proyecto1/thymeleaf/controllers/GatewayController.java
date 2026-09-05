package com.proyecto1.thymeleaf.controllers;

import com.proyecto1.thymeleaf.model.Gateway;
import com.proyecto1.thymeleaf.services.GatewayService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/procesos/{procesoId}/gateways")
public class GatewayController {

    private final GatewayService gatewayService;

    public GatewayController(GatewayService gatewayService) {
        this.gatewayService = gatewayService;
    }

    private final Long EMPRESA_ID_MOCK = 1L;

    // 1. Vista para listar todos los gateways de un proceso
    @GetMapping
    public String listarGateways(@PathVariable Long procesoId, Model model) {
        List<Gateway> gateways = gatewayService.listarPorProcesoYEmpresa(procesoId, EMPRESA_ID_MOCK);
        
        model.addAttribute("gateways", gateways);
        model.addAttribute("procesoId", procesoId);
        return "gateways/lista";
    }

    // 2. Crear un nuevo gateway: mostrar formulario
    @GetMapping("/nuevo")
    public String mostrarFormularioCrear(@PathVariable Long procesoId, Model model) {
        model.addAttribute("gateway", new Gateway());
        model.addAttribute("tiposGateway", Gateway.TipoGateway.values());
        model.addAttribute("procesoId", procesoId);
        return "gateways/formulario";

    // 3. Crear
    @PostMapping("/guardar")
    public String guardarGateway(@PathVariable Long procesoId, 
                                @ModelAttribute("gateway") Gateway gateway, 
                                RedirectAttributes redirectAttributes) {
        try {
            gatewayService.crearGateway(gateway, procesoId, EMPRESA_ID_MOCK);
            redirectAttributes.addFlashAttribute("mensajeExito", "Compuerta creada con éxito.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
        }
        return "redirect:/procesos/" + procesoId + "/gateways";
    }

    // 4. Formulario para editar un gateway existente
    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable Long procesoId, 
                                        @PathVariable Long id, 
                                        Model model) {
        Gateway gateway = gatewayService.obtenerPorIdYEmpresa(id, EMPRESA_ID_MOCK);
        model.addAttribute("gateway", gateway);
        model.addAttribute("tiposGateway", Gateway.TipoGateway.values());
        model.addAttribute("procesoId", procesoId);
        return "gateways/formulario";
    }

    // 5. Actualizar
    @PostMapping("/actualizar/{id}")
    public String actualizarGateway(@PathVariable Long procesoId, 
                                    @PathVariable Long id, 
                                    @ModelAttribute("gateway") Gateway gateway, 
                                    RedirectAttributes redirectAttributes) {
        try {
            gatewayService.actualizarGateway(id, gateway, EMPRESA_ID_MOCK);
            redirectAttributes.addFlashAttribute("mensajeExito", "Compuerta actualizada con éxito.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", e.getMessage());
        }
        return "redirect:/procesos/" + procesoId + "/gateways";
    }

    // 6. Eliminar
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
}
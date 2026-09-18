package com.proyecto1.thymeleaf.controllers;

import com.proyecto1.thymeleaf.dto.GatewayDTO;
import com.proyecto1.thymeleaf.dto.GatewayRespuestaDTO;
import com.proyecto1.thymeleaf.security.ContextoSeguridad;
import com.proyecto1.thymeleaf.services.GatewayService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/procesos/{procesoId}/gateways")
public class GatewayController {

    private final GatewayService gatewayService;

    public GatewayController(GatewayService gatewayService) {
        this.gatewayService = gatewayService;
    }

    @GetMapping
    public ResponseEntity<List<GatewayRespuestaDTO>> listar(@PathVariable Long procesoId) {
        return ResponseEntity.ok(gatewayService
                .listarPorProcesoYEmpresa(procesoId, ContextoSeguridad.empresaIdActual())
                .stream().map(GatewayRespuestaDTO::desde).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<GatewayRespuestaDTO> obtener(@PathVariable Long procesoId, @PathVariable Long id) {
        return ResponseEntity.ok(GatewayRespuestaDTO.desde(
                gatewayService.obtenerPorIdYEmpresa(id, ContextoSeguridad.empresaIdActual())));
    }

    @PostMapping
    public ResponseEntity<GatewayRespuestaDTO> crear(@PathVariable Long procesoId, @Valid @RequestBody GatewayDTO datos) {
        return ResponseEntity.status(HttpStatus.CREATED).body(GatewayRespuestaDTO.desde(
                gatewayService.crearGateway(datos, procesoId, ContextoSeguridad.empresaIdActual())));
    }

    @PutMapping("/{id}")
    public ResponseEntity<GatewayRespuestaDTO> actualizar(@PathVariable Long procesoId, @PathVariable Long id,
                                                          @Valid @RequestBody GatewayDTO datos) {
        return ResponseEntity.ok(GatewayRespuestaDTO.desde(
                gatewayService.actualizarGateway(id, datos, ContextoSeguridad.empresaIdActual())));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long procesoId, @PathVariable Long id) {
        gatewayService.eliminarGateway(id, ContextoSeguridad.empresaIdActual());
        return ResponseEntity.noContent().build();
    }
}

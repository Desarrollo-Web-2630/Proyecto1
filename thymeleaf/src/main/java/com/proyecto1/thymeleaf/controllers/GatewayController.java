package com.proyecto1.thymeleaf.controllers;


import com.proyecto1.thymeleaf.dto.GatewayDTO;
import com.proyecto1.thymeleaf.model.Gateway;
import com.proyecto1.thymeleaf.services.GatewayService;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/procesos/{procesoId}/gateways")
public class GatewayController {

    private final GatewayService gatewayService;
    private final ModelMapper modelMapper;
    private final Long EMPRESA_ID_MOCK = 1L;

    public GatewayController(GatewayService gatewayService, ModelMapper modelMapper) {
        this.gatewayService = gatewayService;
        this.modelMapper = modelMapper;
    }

    @GetMapping
    public ResponseEntity<List<Gateway>> listar(@PathVariable Long procesoId) {
        return ResponseEntity.ok(gatewayService.listarPorProcesoYEmpresa(procesoId, EMPRESA_ID_MOCK));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Gateway> obtener(@PathVariable Long procesoId, @PathVariable Long id) {
        return ResponseEntity.ok(gatewayService.obtenerPorIdYEmpresa(id, EMPRESA_ID_MOCK));
    }

    @PostMapping
    public ResponseEntity<Gateway> crear(@PathVariable Long procesoId, @Valid @RequestBody GatewayDTO datos) {
        Gateway creado = gatewayService.crearGateway(datos, procesoId, EMPRESA_ID_MOCK);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Gateway> actualizar(@PathVariable Long procesoId, @PathVariable Long id,
                                              @Valid @RequestBody GatewayDTO datos) {
        Gateway actualizado = gatewayService.actualizarGateway(id, datos, EMPRESA_ID_MOCK);
        return ResponseEntity.ok(actualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long procesoId, @PathVariable Long id) {
        gatewayService.eliminarGateway(id, EMPRESA_ID_MOCK);
        return ResponseEntity.noContent().build();
    }
}

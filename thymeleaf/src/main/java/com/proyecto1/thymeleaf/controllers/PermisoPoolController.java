package com.proyecto1.thymeleaf.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.proyecto1.thymeleaf.dto.PermisoPoolDTO;
import com.proyecto1.thymeleaf.services.PermisoPoolService;
import com.proyecto1.thymeleaf.util.EmpresaActual;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/procesos/{procesoId}/pools/{poolId}/permisos")
public class PermisoPoolController {

    private final PermisoPoolService permisoPoolService;

    public PermisoPoolController(PermisoPoolService permisoPoolService) {
        this.permisoPoolService = permisoPoolService;
    }

    @GetMapping
    public ResponseEntity<List<PermisoPoolDTO>> listar(@PathVariable Long procesoId, @PathVariable Long poolId) {
        return ResponseEntity.ok(permisoPoolService.listar(poolId, procesoId, EmpresaActual.id()));
    }

    @PutMapping
    public ResponseEntity<List<PermisoPoolDTO>> guardar(@PathVariable Long procesoId, @PathVariable Long poolId,
            @Valid @RequestBody PermisoPoolDTO datos) {
        return ResponseEntity.ok(permisoPoolService.guardar(poolId, procesoId, datos, EmpresaActual.id()));
    }
}

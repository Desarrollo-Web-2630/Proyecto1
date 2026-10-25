package com.proyecto1.thymeleaf.controllers;

import com.proyecto1.thymeleaf.dto.PoolDTO;
import com.proyecto1.thymeleaf.dto.PoolRespuestaDTO;
import com.proyecto1.thymeleaf.util.EmpresaActual;
import com.proyecto1.thymeleaf.services.PoolService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// HU-21: endpoints de pools dentro de un proceso.
@RestController
@RequestMapping("/api/v1/procesos/{procesoId}/pools")
public class PoolController {

    private final PoolService poolService;

    public PoolController(PoolService poolService) {
        this.poolService = poolService;
    }

    @GetMapping
    public ResponseEntity<List<PoolRespuestaDTO>> listar(@PathVariable Long procesoId) {
        return ResponseEntity.ok(poolService
                .listarPorProcesoYEmpresa(procesoId, EmpresaActual.id())
                .stream().map(PoolRespuestaDTO::desde).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PoolRespuestaDTO> obtener(@PathVariable Long procesoId, @PathVariable Long id) {
        return ResponseEntity.ok(PoolRespuestaDTO.desde(
                poolService.obtenerPorIdYEmpresa(id, procesoId, EmpresaActual.id())));
    }

    @PostMapping
    public ResponseEntity<PoolRespuestaDTO> crear(@PathVariable Long procesoId, @Valid @RequestBody PoolDTO datos) {
        return ResponseEntity.status(HttpStatus.CREATED).body(PoolRespuestaDTO.desde(
                poolService.crearPool(datos, procesoId, EmpresaActual.id())));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PoolRespuestaDTO> actualizar(@PathVariable Long procesoId, @PathVariable Long id,
                                                       @Valid @RequestBody PoolDTO datos) {
        return ResponseEntity.ok(PoolRespuestaDTO.desde(
                poolService.actualizarPool(id, datos, procesoId, EmpresaActual.id())));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long procesoId, @PathVariable Long id) {
        poolService.eliminarPool(id, procesoId, EmpresaActual.id());
        return ResponseEntity.noContent().build();
    }
}

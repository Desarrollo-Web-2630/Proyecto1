package com.proyecto1.thymeleaf.controllers;

import com.proyecto1.thymeleaf.dto.LaneDTO;
import com.proyecto1.thymeleaf.dto.LaneRespuestaDTO;
import com.proyecto1.thymeleaf.dto.OrdenLanesDTO;
import com.proyecto1.thymeleaf.util.EmpresaActual;
import com.proyecto1.thymeleaf.services.LaneService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// HU-22: endpoints de lanes dentro de un pool.
@RestController
@RequestMapping("/api/v1/procesos/{procesoId}/pools/{poolId}/lanes")
public class LaneController {

    private final LaneService laneService;

    public LaneController(LaneService laneService) {
        this.laneService = laneService;
    }

    @GetMapping
    public ResponseEntity<List<LaneRespuestaDTO>> listar(@PathVariable Long procesoId, @PathVariable Long poolId) {
        return ResponseEntity.ok(laneService
                .listarPorPoolYEmpresa(poolId, procesoId, EmpresaActual.id())
                .stream().map(LaneRespuestaDTO::desde).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<LaneRespuestaDTO> obtener(@PathVariable Long procesoId, @PathVariable Long poolId,
                                                    @PathVariable Long id) {
        return ResponseEntity.ok(LaneRespuestaDTO.desde(
                laneService.obtenerPorIdYEmpresa(id, poolId, EmpresaActual.id())));
    }

    @PostMapping
    public ResponseEntity<LaneRespuestaDTO> crear(@PathVariable Long procesoId, @PathVariable Long poolId,
                                                  @Valid @RequestBody LaneDTO datos) {
        return ResponseEntity.status(HttpStatus.CREATED).body(LaneRespuestaDTO.desde(
                laneService.crearLane(datos, poolId, procesoId, EmpresaActual.id())));
    }

    @PutMapping("/{id}")
    public ResponseEntity<LaneRespuestaDTO> actualizar(@PathVariable Long procesoId, @PathVariable Long poolId,
                                                       @PathVariable Long id, @Valid @RequestBody LaneDTO datos) {
        return ResponseEntity.ok(LaneRespuestaDTO.desde(
                laneService.actualizarLane(id, datos, poolId, EmpresaActual.id())));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long procesoId, @PathVariable Long poolId,
                                         @PathVariable Long id) {
        laneService.eliminarLane(id, poolId, EmpresaActual.id());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/orden")
    public ResponseEntity<List<LaneRespuestaDTO>> reordenar(@PathVariable Long procesoId, @PathVariable Long poolId,
                                                            @Valid @RequestBody OrdenLanesDTO datos) {
        return ResponseEntity.ok(laneService
                .reordenarLanes(poolId, datos.lanesIds(), procesoId, EmpresaActual.id())
                .stream().map(LaneRespuestaDTO::desde).toList());
    }
}

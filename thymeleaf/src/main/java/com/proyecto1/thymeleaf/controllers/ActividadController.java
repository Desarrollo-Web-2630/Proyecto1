package com.proyecto1.thymeleaf.controllers;

import com.proyecto1.thymeleaf.dto.ActividadRequestDTO;
import com.proyecto1.thymeleaf.dto.ActividadResponseDTO;
import com.proyecto1.thymeleaf.services.ActividadService;
import com.proyecto1.thymeleaf.util.EmpresaActual;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/procesos/{procesoId}/actividades")
public class ActividadController {

    private final ActividadService actividadService;

    public ActividadController(ActividadService actividadService) {
        this.actividadService = actividadService;
    }

    @GetMapping
    public ResponseEntity<List<ActividadResponseDTO>> listar(@PathVariable Long procesoId) {
        return ResponseEntity.ok(actividadService.listarPorProcesoYEmpresa(procesoId, EmpresaActual.id()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ActividadResponseDTO> obtener(@PathVariable Long procesoId, @PathVariable Long id) {
        return ResponseEntity.ok(actividadService.obtenerPorIdYEmpresa(id, EmpresaActual.id()));
    }

    @PostMapping
    public ResponseEntity<ActividadResponseDTO> crear(@PathVariable Long procesoId,
                                                       @Valid @RequestBody ActividadRequestDTO datos) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(actividadService.crearActividad(datos, procesoId, EmpresaActual.id()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ActividadResponseDTO> actualizar(@PathVariable Long procesoId, @PathVariable Long id,
                                                           @Valid @RequestBody ActividadRequestDTO datos) {
        return ResponseEntity.ok(actividadService.actualizarActividad(id, datos, EmpresaActual.id()));
    }

    @PostMapping("/{id}/mover")
    public ResponseEntity<ActividadResponseDTO> mover(@PathVariable Long procesoId, @PathVariable Long id,
                                                       @RequestParam Integer posicionX, @RequestParam Integer posicionY) {
        return ResponseEntity.ok(actividadService.moverActividad(id, posicionX, posicionY, EmpresaActual.id()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long procesoId, @PathVariable Long id) {
        actividadService.eliminarActividad(id, EmpresaActual.id());
        return ResponseEntity.noContent().build();
    }
}

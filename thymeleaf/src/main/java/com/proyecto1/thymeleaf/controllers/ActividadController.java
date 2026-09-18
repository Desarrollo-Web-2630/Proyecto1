package com.proyecto1.thymeleaf.controllers;

import com.proyecto1.thymeleaf.dto.ActividadDTO;
import com.proyecto1.thymeleaf.dto.ActividadRespuestaDTO;
import com.proyecto1.thymeleaf.security.ContextoSeguridad;
import com.proyecto1.thymeleaf.services.ActividadService;
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
    public ResponseEntity<List<ActividadRespuestaDTO>> listar(@PathVariable Long procesoId) {
        return ResponseEntity.ok(actividadService
                .listarPorProcesoYEmpresa(procesoId, ContextoSeguridad.empresaIdActual())
                .stream().map(ActividadRespuestaDTO::desde).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ActividadRespuestaDTO> obtener(@PathVariable Long procesoId, @PathVariable Long id) {
        return ResponseEntity.ok(ActividadRespuestaDTO.desde(
                actividadService.obtenerPorIdYEmpresa(id, ContextoSeguridad.empresaIdActual())));
    }

    @PostMapping
    public ResponseEntity<ActividadRespuestaDTO> crear(@PathVariable Long procesoId, @Valid @RequestBody ActividadDTO datos) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ActividadRespuestaDTO.desde(
                actividadService.crearActividad(datos, procesoId, ContextoSeguridad.empresaIdActual())));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ActividadRespuestaDTO> actualizar(@PathVariable Long procesoId, @PathVariable Long id,
                                                            @Valid @RequestBody ActividadDTO datos) {
        return ResponseEntity.ok(ActividadRespuestaDTO.desde(
                actividadService.actualizarActividad(id, datos, ContextoSeguridad.empresaIdActual())));
    }

    @PostMapping("/{id}/mover")
    public ResponseEntity<ActividadRespuestaDTO> mover(@PathVariable Long procesoId, @PathVariable Long id,
                                                       @RequestParam Integer posicionX, @RequestParam Integer posicionY) {
        return ResponseEntity.ok(ActividadRespuestaDTO.desde(
                actividadService.moverActividad(id, posicionX, posicionY, ContextoSeguridad.empresaIdActual())));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long procesoId, @PathVariable Long id) {
        actividadService.eliminarActividad(id, ContextoSeguridad.empresaIdActual());
        return ResponseEntity.noContent().build();
    }
}

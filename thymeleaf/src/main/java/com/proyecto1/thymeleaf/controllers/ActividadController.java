package com.proyecto1.thymeleaf.controllers;

import com.proyecto1.thymeleaf.dto.ActividadDTO;
import com.proyecto1.thymeleaf.model.Actividad;
import com.proyecto1.thymeleaf.services.ActividadService;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/procesos/{procesoId}/actividades")
public class ActividadController {

    private final ActividadService actividadService;
    private final ModelMapper modelMapper;
    private final Long EMPRESA_ID_MOCK = 1L;

    public ActividadController(ActividadService actividadService, ModelMapper modelMapper) {
        this.actividadService = actividadService;
        this.modelMapper = modelMapper;
    }

    @GetMapping
    public ResponseEntity<List<Actividad>> listar(@PathVariable Long procesoId) {
        return ResponseEntity.ok(actividadService.listarPorProcesoYEmpresa(procesoId, EMPRESA_ID_MOCK));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Actividad> obtener(@PathVariable Long procesoId, @PathVariable Long id) {
        return ResponseEntity.ok(actividadService.obtenerPorIdYEmpresa(id, EMPRESA_ID_MOCK));
    }

    @PostMapping
    public ResponseEntity<Actividad> crear(@PathVariable Long procesoId, @Valid @RequestBody ActividadDTO datos) {
        Actividad creado = actividadService.crearActividad(datos, procesoId, EMPRESA_ID_MOCK);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Actividad> actualizar(@PathVariable Long procesoId, @PathVariable Long id,
                                                @Valid @RequestBody ActividadDTO datos) {
        Actividad actualizado = actividadService.actualizarActividad(id, datos, EMPRESA_ID_MOCK);
        return ResponseEntity.ok(actualizado);
    }

    @PostMapping("/{id}/mover")
    public ResponseEntity<Actividad> mover(@PathVariable Long procesoId, @PathVariable Long id,
                                        @RequestParam Integer posicionX, @RequestParam Integer posicionY) {
        Actividad movido = actividadService.moverActividad(id, posicionX, posicionY, EMPRESA_ID_MOCK);
        return ResponseEntity.ok(movido);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long procesoId, @PathVariable Long id) {
        actividadService.eliminarActividad(id, EMPRESA_ID_MOCK);
        return ResponseEntity.noContent().build();
    }
}

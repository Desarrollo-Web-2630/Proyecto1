package com.proyecto1.thymeleaf.controllers;

import com.proyecto1.thymeleaf.dto.ArcoDTO;
import com.proyecto1.thymeleaf.model.Arco;
import com.proyecto1.thymeleaf.services.ArcoService;
import com.proyecto1.thymeleaf.services.ElementoConectableService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/procesos/{procesoId}/arcos")
public class ArcoController {

    private final ArcoService arcoService;
    private final ElementoConectableService elementoConectableService;
    private final Long EMPRESA_ID_MOCK = 1L;

    public ArcoController(ArcoService arcoService, ElementoConectableService elementoConectableService) {
        this.arcoService = arcoService;
        this.elementoConectableService = elementoConectableService;
    }

    @GetMapping
    public ResponseEntity<List<Arco>> listar(@PathVariable Long procesoId) {
        return ResponseEntity.ok(arcoService.listarPorProcesoYEmpresa(procesoId, EMPRESA_ID_MOCK));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Arco> obtener(@PathVariable Long procesoId, @PathVariable Long id) {
        return ResponseEntity.ok(arcoService.obtenerPorIdYEmpresa(id, EMPRESA_ID_MOCK));
    }

    @PostMapping
    public ResponseEntity<Arco> crear(@PathVariable Long procesoId, @Valid @RequestBody ArcoDTO datos) {
        Arco creado = arcoService.crearArco(datos, procesoId, EMPRESA_ID_MOCK);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Arco> actualizar(@PathVariable Long procesoId, @PathVariable Long id,
                                           @Valid @RequestBody ArcoDTO datos) {
        Arco actualizado = arcoService.actualizarArco(id, datos, EMPRESA_ID_MOCK);
        return ResponseEntity.ok(actualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> eliminar(@PathVariable Long procesoId, @PathVariable Long id) {
        String advertencia = arcoService.advertenciaAlEliminar(id, EMPRESA_ID_MOCK);
        arcoService.eliminarArco(id, EMPRESA_ID_MOCK);
        Map<String, String> response = new HashMap<>();
        response.put("mensaje", "Arco eliminado correctamente.");
        if (advertencia != null) {
            response.put("advertencia", advertencia);
        }
        return ResponseEntity.ok(response);
    }
}

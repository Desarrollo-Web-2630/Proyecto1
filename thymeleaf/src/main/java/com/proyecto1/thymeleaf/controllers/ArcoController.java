package com.proyecto1.thymeleaf.controllers;

import com.proyecto1.thymeleaf.dto.ArcoDTO;
import com.proyecto1.thymeleaf.dto.ArcoRespuestaDTO;
import com.proyecto1.thymeleaf.util.EmpresaActual;
import com.proyecto1.thymeleaf.services.ArcoService;
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

    public ArcoController(ArcoService arcoService) {
        this.arcoService = arcoService;
    }

    @GetMapping
    public ResponseEntity<List<ArcoRespuestaDTO>> listar(@PathVariable Long procesoId) {
        return ResponseEntity.ok(arcoService
                .listarPorProcesoYEmpresa(procesoId, EmpresaActual.id())
                .stream().map(ArcoRespuestaDTO::desde).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ArcoRespuestaDTO> obtener(@PathVariable Long procesoId, @PathVariable Long id) {
        return ResponseEntity.ok(ArcoRespuestaDTO.desde(
                arcoService.obtenerPorIdYEmpresa(id, EmpresaActual.id())));
    }

    @PostMapping
    public ResponseEntity<ArcoRespuestaDTO> crear(@PathVariable Long procesoId, @Valid @RequestBody ArcoDTO datos) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ArcoRespuestaDTO.desde(
                arcoService.crearArco(datos, procesoId, EmpresaActual.id())));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ArcoRespuestaDTO> actualizar(@PathVariable Long procesoId, @PathVariable Long id,
                                                       @Valid @RequestBody ArcoDTO datos) {
        return ResponseEntity.ok(ArcoRespuestaDTO.desde(
                arcoService.actualizarArco(id, datos, EmpresaActual.id())));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> eliminar(@PathVariable Long procesoId, @PathVariable Long id) {
        Long empresaId = EmpresaActual.id();
        String advertencia = arcoService.advertenciaAlEliminar(id, empresaId);
        arcoService.eliminarArco(id, empresaId);
        Map<String, String> response = new HashMap<>();
        response.put("mensaje", "Arco eliminado correctamente.");
        if (advertencia != null) {
            response.put("advertencia", advertencia);
        }
        return ResponseEntity.ok(response);
    }
}

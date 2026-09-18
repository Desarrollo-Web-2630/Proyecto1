package com.proyecto1.thymeleaf.controllers;

import com.proyecto1.thymeleaf.model.ElementoConectable;
import com.proyecto1.thymeleaf.security.ContextoSeguridad;
import com.proyecto1.thymeleaf.services.ElementoConectableService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/procesos/{procesoId}/elementos")
public class ElementoConectableController {

    private final ElementoConectableService elementoConectableService;

    public ElementoConectableController(ElementoConectableService elementoConectableService) {
        this.elementoConectableService = elementoConectableService;
    }

    // 1. READ ALL - Obtener todos los elementos conectables de un proceso
    @GetMapping
    public ResponseEntity<List<ElementoConectable>> listarPorProceso(@PathVariable Long procesoId) {
        List<ElementoConectable> elementos = elementoConectableService
                .listarElementosPorProcesoYEmpresa(procesoId, ContextoSeguridad.empresaIdActual());
        return ResponseEntity.ok(elementos);
    }

    // 2. READ ONE - Obtener un elemento conectable por ID
    @GetMapping("/{id}")
    public ResponseEntity<ElementoConectable> obtenerPorId(@PathVariable Long procesoId,
                                                        @PathVariable Long id) {
        ElementoConectable elemento = elementoConectableService
                .obtenerPorIdYEmpresa(id, ContextoSeguridad.empresaIdActual());
        return ResponseEntity.ok(elemento);
    }

    // 3. UPDATE POSITION - Actualizar coordenadas X e Y
    @PatchMapping("/{id}/posicion")
    public ResponseEntity<?> actualizarPosicion(@PathVariable Long procesoId,
                                                @PathVariable Long id,
                                                @RequestParam Integer x,
                                                @RequestParam Integer y) {
        elementoConectableService.actualizarPosiciones(id, x, y, ContextoSeguridad.empresaIdActual());
        return ResponseEntity.ok().build();
    }

    // 4. DELETE - Eliminar un elemento (Soft delete gestionado en el servicio)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long procesoId,
                                        @PathVariable Long id) {
        elementoConectableService.eliminarElemento(id, ContextoSeguridad.empresaIdActual());
        return ResponseEntity.noContent().build();
    }
}

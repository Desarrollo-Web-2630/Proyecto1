package com.proyecto1.thymeleaf.controllers;

import com.proyecto1.thymeleaf.model.ElementoConectable;
import com.proyecto1.thymeleaf.services.ElementoConectableService;
import org.springframework.http.HttpStatus;
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

    // ID de empresa simulado (reemplazar por ID extraído del Token JWT o sesión)
    private final Long EMPRESA_ID_MOCK = 1L;

    // 1. READ ALL - Obtener todos los elementos conectables de un proceso
    @GetMapping
    public ResponseEntity<List<ElementoConectable>> listarPorProceso(@PathVariable Long procesoId) {
        List<ElementoConectable> elementos = elementoConectableService
                .listarElementosPorProcesoYEmpresa(procesoId, EMPRESA_ID_MOCK);
        return ResponseEntity.ok(elementos);
    }

    // 2. READ ONE - Obtener un elemento conectable por ID
    @GetMapping("/{id}")
    public ResponseEntity<ElementoConectable> obtenerPorId(@PathVariable Long procesoId, 
                                                        @PathVariable Long id) {
        ElementoConectable elemento = elementoConectableService
                .obtenerPorIdYEmpresa(id, EMPRESA_ID_MOCK);
        return ResponseEntity.ok(elemento);
    }

    // 3. UPDATE POSITION - Actualizar coordenadas X e Y
    @PatchMapping("/{id}/posicion")
    public ResponseEntity<?> actualizarPosicion(@PathVariable Long procesoId,
                                                @PathVariable Long id,
                                                @RequestParam Integer x,
                                                @RequestParam Integer y) {
        elementoConectableService.actualizarPosiciones(id, x, y, EMPRESA_ID_MOCK);
        return ResponseEntity.ok().build();
    }

    // 4. DELETE - Eliminar un elemento (Soft delete gestionado en el servicio)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long procesoId, 
                                        @PathVariable Long id) {
        elementoConectableService.eliminarElemento(id, EMPRESA_ID_MOCK);
        return ResponseEntity.noContent().build();
    }
}
package com.proyecto1.thymeleaf.controllers;

import com.proyecto1.thymeleaf.dto.ProcesoDTO;
import com.proyecto1.thymeleaf.model.Proceso;
import com.proyecto1.thymeleaf.services.ProcesoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/procesos")
public class ProcesoController {

    private final ProcesoService procesoService;
    private final Long EMPRESA_ID_MOCK = 1L;

    public ProcesoController(ProcesoService procesoService) {
        this.procesoService = procesoService;
    }

    @GetMapping
    public ResponseEntity<List<Proceso>> listar() {
        List<Proceso> procesos = procesoService.listarPorEmpresa(EMPRESA_ID_MOCK);
        return ResponseEntity.ok(procesos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Proceso> obtener(@PathVariable Long id) {
        Proceso proceso = procesoService.obtenerPorIdYEmpresa(id, EMPRESA_ID_MOCK);
        return ResponseEntity.ok(proceso);
    }

    @PostMapping
    public ResponseEntity<Proceso> crear(@RequestBody ProcesoDTO datos) {
        Proceso creado = procesoService.crearProceso(datos, EMPRESA_ID_MOCK);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Proceso> actualizar(@PathVariable Long id, @RequestBody ProcesoDTO datos) {
        Proceso actualizado = procesoService.actualizarProceso(id, datos, EMPRESA_ID_MOCK);
        return ResponseEntity.ok(actualizado);
    }

    @PostMapping("/{id}/publicar")
    public ResponseEntity<Proceso> publicar(@PathVariable Long id) {
        Proceso publicado = procesoService.publicarProceso(id, EMPRESA_ID_MOCK);
        return ResponseEntity.ok(publicado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        procesoService.eliminarProceso(id, EMPRESA_ID_MOCK);
        return ResponseEntity.noContent().build();
    }
}

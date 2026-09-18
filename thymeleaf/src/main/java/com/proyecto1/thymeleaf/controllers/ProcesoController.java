package com.proyecto1.thymeleaf.controllers;

import com.proyecto1.thymeleaf.dto.ProcesoDTO;
import com.proyecto1.thymeleaf.model.Proceso;
import com.proyecto1.thymeleaf.security.ContextoSeguridad;
import com.proyecto1.thymeleaf.services.ProcesoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/procesos")
public class ProcesoController {

    private final ProcesoService procesoService;

    public ProcesoController(ProcesoService procesoService) {
        this.procesoService = procesoService;
    }

    @GetMapping
    public ResponseEntity<List<Proceso>> listar() {
        List<Proceso> procesos = procesoService.listarPorEmpresa(ContextoSeguridad.empresaIdActual());
        return ResponseEntity.ok(procesos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Proceso> obtener(@PathVariable Long id) {
        Proceso proceso = procesoService.obtenerPorIdYEmpresa(id, ContextoSeguridad.empresaIdActual());
        return ResponseEntity.ok(proceso);
    }

    @PostMapping
    public ResponseEntity<Proceso> crear(@Valid @RequestBody ProcesoDTO datos) {
        Proceso creado = procesoService.crearProceso(datos, ContextoSeguridad.empresaIdActual());
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Proceso> actualizar(@PathVariable Long id, @Valid @RequestBody ProcesoDTO datos) {
        Proceso actualizado = procesoService.actualizarProceso(id, datos, ContextoSeguridad.empresaIdActual());
        return ResponseEntity.ok(actualizado);
    }

    @PostMapping("/{id}/publicar")
    public ResponseEntity<Proceso> publicar(@PathVariable Long id) {
        Proceso publicado = procesoService.publicarProceso(id, ContextoSeguridad.empresaIdActual());
        return ResponseEntity.ok(publicado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        procesoService.eliminarProceso(id, ContextoSeguridad.empresaIdActual());
        return ResponseEntity.noContent().build();
    }
}

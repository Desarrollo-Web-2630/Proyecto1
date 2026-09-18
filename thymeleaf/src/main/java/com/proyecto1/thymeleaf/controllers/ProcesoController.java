package com.proyecto1.thymeleaf.controllers;

import com.proyecto1.thymeleaf.dto.ProcesoDTO;
import com.proyecto1.thymeleaf.model.Proceso;
import com.proyecto1.thymeleaf.security.ContextoSeguridad;
import com.proyecto1.thymeleaf.services.ProcesoService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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

    // Activos por defecto; ?incluirInactivos=true para verlos todos (HU-07)
    @GetMapping
    public ResponseEntity<List<Proceso>> listar(@RequestParam(defaultValue = "false") boolean incluirInactivos) {
        List<Proceso> procesos = procesoService.listarPorEmpresa(ContextoSeguridad.empresaIdActual(), incluirInactivos);
        return ResponseEntity.ok(procesos);
    }

    /**
     * HU-07: busqueda por nombre, filtros por estado y categoria, paginacion.
     * Ejemplo: /api/v1/procesos/buscar?nombre=compra&estado=PUBLICADO&page=0&size=10
     */
    @GetMapping("/buscar")
    public ResponseEntity<Page<Proceso>> buscar(@RequestParam(required = false) String nombre,
                                                @RequestParam(required = false) Proceso.EstadoProceso estado,
                                                @RequestParam(required = false) String categoria,
                                                @RequestParam(defaultValue = "false") boolean incluirInactivos,
                                                @RequestParam(defaultValue = "0") int page,
                                                @RequestParam(defaultValue = "10") int size) {
        // Tope de tamano de pagina para que nadie pida 10 millones de filas de golpe
        int tamano = Math.min(Math.max(size, 1), 100);
        Page<Proceso> resultado = procesoService.buscar(
                ContextoSeguridad.empresaIdActual(), nombre, estado, categoria, incluirInactivos,
                PageRequest.of(Math.max(page, 0), tamano, Sort.by("nombre").ascending()));
        return ResponseEntity.ok(resultado);
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

    @PostMapping("/{id}/inactivar")
    public ResponseEntity<Proceso> inactivar(@PathVariable Long id) {
        return ResponseEntity.ok(procesoService.inactivarProceso(id, ContextoSeguridad.empresaIdActual()));
    }

    @PostMapping("/{id}/reactivar")
    public ResponseEntity<Proceso> reactivar(@PathVariable Long id) {
        return ResponseEntity.ok(procesoService.reactivarProceso(id, ContextoSeguridad.empresaIdActual()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        procesoService.eliminarProceso(id, ContextoSeguridad.empresaIdActual());
        return ResponseEntity.noContent().build();
    }
}

package com.proyecto1.thymeleaf.controllers;

import com.proyecto1.thymeleaf.dto.ProcesoDTO;
import com.proyecto1.thymeleaf.dto.ProcesoRespuestaDTO;
import com.proyecto1.thymeleaf.model.Proceso;
import com.proyecto1.thymeleaf.util.EmpresaActual;
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
    public ResponseEntity<List<ProcesoRespuestaDTO>> listar(@RequestParam(defaultValue = "false") boolean incluirInactivos) {
        List<ProcesoRespuestaDTO> procesos = procesoService
                .listarPorEmpresa(EmpresaActual.id(), incluirInactivos)
                .stream().map(ProcesoRespuestaDTO::desde).toList();
        return ResponseEntity.ok(procesos);
    }

    /**
     * HU-07: busqueda por nombre, filtros por estado y categoria, paginacion.
     * Ejemplo: /api/v1/procesos/buscar?nombre=compra&estado=PUBLICADO&page=0&size=10
     */
    @GetMapping("/buscar")
    public ResponseEntity<Page<ProcesoRespuestaDTO>> buscar(@RequestParam(required = false) String nombre,
                                                            @RequestParam(required = false) Proceso.EstadoProceso estado,
                                                            @RequestParam(required = false) String categoria,
                                                            @RequestParam(defaultValue = "false") boolean incluirInactivos,
                                                            @RequestParam(defaultValue = "0") int page,
                                                            @RequestParam(defaultValue = "10") int size) {
        // Tope de tamano de pagina para que nadie pida 10 millones de filas de golpe
        int tamano = Math.min(Math.max(size, 1), 100);
        Page<ProcesoRespuestaDTO> resultado = procesoService.buscar(
                        EmpresaActual.id(), nombre, estado, categoria, incluirInactivos,
                        PageRequest.of(Math.max(page, 0), tamano, Sort.by("nombre").ascending()))
                .map(ProcesoRespuestaDTO::desde);
        return ResponseEntity.ok(resultado);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProcesoRespuestaDTO> obtener(@PathVariable Long id) {
        Proceso proceso = procesoService.obtenerPorIdYEmpresa(id, EmpresaActual.id());
        return ResponseEntity.ok(ProcesoRespuestaDTO.desde(proceso));
    }

    @PostMapping
    public ResponseEntity<ProcesoRespuestaDTO> crear(@Valid @RequestBody ProcesoDTO datos) {
        Proceso creado = procesoService.crearProceso(datos, EmpresaActual.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(ProcesoRespuestaDTO.desde(creado));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProcesoRespuestaDTO> actualizar(@PathVariable Long id, @Valid @RequestBody ProcesoDTO datos) {
        Proceso actualizado = procesoService.actualizarProceso(id, datos, EmpresaActual.id());
        return ResponseEntity.ok(ProcesoRespuestaDTO.desde(actualizado));
    }

    @PostMapping("/{id}/publicar")
    public ResponseEntity<ProcesoRespuestaDTO> publicar(@PathVariable Long id) {
        return ResponseEntity.ok(ProcesoRespuestaDTO.desde(
                procesoService.publicarProceso(id, EmpresaActual.id())));
    }

    @PostMapping("/{id}/inactivar")
    public ResponseEntity<ProcesoRespuestaDTO> inactivar(@PathVariable Long id) {
        return ResponseEntity.ok(ProcesoRespuestaDTO.desde(
                procesoService.inactivarProceso(id, EmpresaActual.id())));
    }

    @PostMapping("/{id}/reactivar")
    public ResponseEntity<ProcesoRespuestaDTO> reactivar(@PathVariable Long id) {
        return ResponseEntity.ok(ProcesoRespuestaDTO.desde(
                procesoService.reactivarProceso(id, EmpresaActual.id())));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        procesoService.eliminarProceso(id, EmpresaActual.id());
        return ResponseEntity.noContent().build();
    }
}

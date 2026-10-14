package com.proyecto1.thymeleaf.controllers;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.proyecto1.thymeleaf.dto.RolProcesoRequestDTO;
import com.proyecto1.thymeleaf.dto.RolProcesoRespuestaDTO;
import com.proyecto1.thymeleaf.services.RolProcesoService;
import com.proyecto1.thymeleaf.util.EmpresaActual;

import jakarta.validation.Valid;

/**
 * Roles de proceso de la empresa (HU-17 a HU-20).
 *
 * El rol de proceso es la funcion que se dibuja como lane; no es el rol de
 * acceso del usuario (Usuario.rolAcceso). Los permisos por rol de acceso
 * ("solo el admin crea y elimina") se aplican cuando exista autenticacion.
 */
@RestController
@RequestMapping("/api/v1/roles-proceso")
public class RolProcesoController {

    private final RolProcesoService rolProcesoService;

    public RolProcesoController(RolProcesoService rolProcesoService) {
        this.rolProcesoService = rolProcesoService;
    }

    // HU-20: catalogo completo de la empresa (para los selectores de lane)
    @GetMapping
    public ResponseEntity<List<RolProcesoRespuestaDTO>> listar() {
        return ResponseEntity.ok(rolProcesoService.listarPorEmpresa(EmpresaActual.id()));
    }

    /**
     * HU-20: busqueda por nombre y paginacion.
     * Ejemplo: /api/v1/roles-proceso/buscar?nombre=anal&page=0&size=10
     */
    @GetMapping("/buscar")
    public ResponseEntity<Page<RolProcesoRespuestaDTO>> buscar(@RequestParam(required = false) String nombre,
                                                               @RequestParam(defaultValue = "0") int page,
                                                               @RequestParam(defaultValue = "10") int size) {
        // Tope de tamano de pagina para que nadie pida 10 millones de filas de golpe
        int tamano = Math.min(Math.max(size, 1), 100);
        return ResponseEntity.ok(rolProcesoService.buscar(EmpresaActual.id(), nombre,
                PageRequest.of(Math.max(page, 0), tamano, Sort.by("nombre").ascending())));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RolProcesoRespuestaDTO> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(rolProcesoService.obtenerPorIdYEmpresa(id, EmpresaActual.id()));
    }

    // HU-17
    @PostMapping
    public ResponseEntity<RolProcesoRespuestaDTO> crear(@Valid @RequestBody RolProcesoRequestDTO datos) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(rolProcesoService.crearRol(datos, EmpresaActual.id()));
    }

    // HU-18
    @PutMapping("/{id}")
    public ResponseEntity<RolProcesoRespuestaDTO> actualizar(@PathVariable Long id,
                                                             @Valid @RequestBody RolProcesoRequestDTO datos) {
        return ResponseEntity.ok(rolProcesoService.actualizarRol(id, datos, EmpresaActual.id()));
    }

    // HU-19: 204 si se elimina; 400 con el detalle si el rol esta en uso
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        rolProcesoService.eliminarRol(id, EmpresaActual.id());
        return ResponseEntity.noContent().build();
    }
}

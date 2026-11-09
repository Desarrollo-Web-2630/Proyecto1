package com.proyecto1.thymeleaf.controllers;

import com.proyecto1.thymeleaf.dto.EmpresaDTO;
import com.proyecto1.thymeleaf.dto.EmpresaRespuestaDTO;
import com.proyecto1.thymeleaf.exceptions.RecursoNoEncontradoException;
import com.proyecto1.thymeleaf.services.EmpresaService;
import com.proyecto1.thymeleaf.util.EmpresaActual;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/empresas")
public class EmpresaController {

    private final EmpresaService empresaService;

    public EmpresaController(EmpresaService empresaService) {
        this.empresaService = empresaService;
    }

    // Aislamiento: cada usuario solo ve su propia empresa, nunca el listado de todas
    @GetMapping
    public ResponseEntity<List<EmpresaRespuestaDTO>> listar() {
        return ResponseEntity.ok(List.of(EmpresaRespuestaDTO.desde(empresaService.obtenerPorId(EmpresaActual.id()))));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmpresaRespuestaDTO> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(EmpresaRespuestaDTO.desde(empresaService.obtenerPorId(propia(id))));
    }

    // Publico: registra la empresa y su administrador inicial (inactivo hasta activar la cuenta)
    @PostMapping
    public ResponseEntity<EmpresaRespuestaDTO> crear(@Valid @RequestBody EmpresaDTO datos) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(EmpresaRespuestaDTO.desde(empresaService.registrarEmpresa(datos)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmpresaRespuestaDTO> actualizar(@PathVariable Long id, @Valid @RequestBody EmpresaDTO datos) {
        return ResponseEntity.ok(EmpresaRespuestaDTO.desde(empresaService.actualizarEmpresa(propia(id), datos)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        empresaService.eliminarEmpresa(propia(id));
        return ResponseEntity.noContent().build();
    }

    // Una empresa distinta a la del token responde 404, igual que si no existiera
    private static Long propia(Long id) {
        if (!EmpresaActual.id().equals(id)) {
            throw new RecursoNoEncontradoException("La empresa no existe");
        }
        return id;
    }
}

package com.proyecto1.thymeleaf.controllers;

import com.proyecto1.thymeleaf.dto.EmpresaDTO;
import com.proyecto1.thymeleaf.dto.EmpresaRespuestaDTO;
import com.proyecto1.thymeleaf.services.EmpresaService;
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

    @GetMapping
    public ResponseEntity<List<EmpresaRespuestaDTO>> listar() {
        return ResponseEntity.ok(empresaService.listarTodas().stream().map(EmpresaRespuestaDTO::desde).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmpresaRespuestaDTO> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(EmpresaRespuestaDTO.desde(empresaService.obtenerPorId(id)));
    }

    // Publico: registra la empresa y su administrador inicial (inactivo hasta activar la cuenta)
    @PostMapping
    public ResponseEntity<EmpresaRespuestaDTO> crear(@Valid @RequestBody EmpresaDTO datos) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(EmpresaRespuestaDTO.desde(empresaService.registrarEmpresa(datos)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmpresaRespuestaDTO> actualizar(@PathVariable Long id, @Valid @RequestBody EmpresaDTO datos) {
        return ResponseEntity.ok(EmpresaRespuestaDTO.desde(empresaService.actualizarEmpresa(id, datos)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        empresaService.eliminarEmpresa(id);
        return ResponseEntity.noContent().build();
    }
}

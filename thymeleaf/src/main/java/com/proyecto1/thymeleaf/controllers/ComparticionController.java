package com.proyecto1.thymeleaf.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.proyecto1.thymeleaf.dto.ComparticionRequestDTO;
import com.proyecto1.thymeleaf.dto.ComparticionResponseDTO;
import com.proyecto1.thymeleaf.services.ComparticionService;
import com.proyecto1.thymeleaf.util.EmpresaActual;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/procesos/{id}/comparticion")
public class ComparticionController {

    private final ComparticionService comparticionService;

    public ComparticionController(ComparticionService comparticionService) {
        this.comparticionService = comparticionService;
    }

    @GetMapping
    public ResponseEntity<ComparticionResponseDTO> listar(@PathVariable Long id) {
        return ResponseEntity.ok(comparticionService.listar(id, EmpresaActual.id()));
    }

    @PutMapping
    public ResponseEntity<ComparticionResponseDTO> actualizar(@PathVariable Long id,
            @Valid @RequestBody ComparticionRequestDTO datos) {
        return ResponseEntity.ok(comparticionService.actualizar(id, datos, EmpresaActual.id()));
    }
}

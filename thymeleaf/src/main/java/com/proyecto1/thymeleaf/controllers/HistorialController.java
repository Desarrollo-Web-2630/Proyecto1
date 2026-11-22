package com.proyecto1.thymeleaf.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.proyecto1.thymeleaf.dto.HistorialCambioDTO;
import com.proyecto1.thymeleaf.services.AuditoriaService;
import com.proyecto1.thymeleaf.util.EmpresaActual;

@RestController
@RequestMapping("/api/v1")
public class HistorialController {

    private final AuditoriaService auditoriaService;

    public HistorialController(AuditoriaService auditoriaService) {
        this.auditoriaService = auditoriaService;
    }

    @GetMapping("/historial")
    public ResponseEntity<List<HistorialCambioDTO>> historialDeEmpresa() {
        return ResponseEntity.ok(auditoriaService.historialDeEmpresa(EmpresaActual.id()));
    }

    @GetMapping("/procesos/{id}/historial")
    public ResponseEntity<List<HistorialCambioDTO>> historialDeProceso(@PathVariable Long id) {
        return ResponseEntity.ok(auditoriaService.historialDeEntidad("Proceso", id, EmpresaActual.id()));
    }
}

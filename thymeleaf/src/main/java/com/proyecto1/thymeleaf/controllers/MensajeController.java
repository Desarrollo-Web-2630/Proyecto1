package com.proyecto1.thymeleaf.controllers;

import com.proyecto1.thymeleaf.dto.MensajeDTO;
import com.proyecto1.thymeleaf.dto.MensajeRespuestaDTO;
import com.proyecto1.thymeleaf.util.EmpresaActual;
import com.proyecto1.thymeleaf.services.MensajeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// HU-25 a HU-28: endpoints de mensajes entre pools de un proceso.
@RestController
@RequestMapping("/api/v1/procesos/{procesoId}/mensajes")
public class MensajeController {

    private final MensajeService mensajeService;

    public MensajeController(MensajeService mensajeService) {
        this.mensajeService = mensajeService;
    }

    @GetMapping
    public ResponseEntity<List<MensajeRespuestaDTO>> listar(@PathVariable Long procesoId) {
        return ResponseEntity.ok(mensajeService
                .listarPorProcesoYEmpresa(procesoId, EmpresaActual.id())
                .stream().map(MensajeRespuestaDTO::desde).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MensajeRespuestaDTO> obtener(@PathVariable Long procesoId, @PathVariable Long id) {
        return ResponseEntity.ok(MensajeRespuestaDTO.desde(
                mensajeService.obtenerPorIdYEmpresa(id, procesoId, EmpresaActual.id())));
    }

    @PostMapping
    public ResponseEntity<MensajeRespuestaDTO> crear(@PathVariable Long procesoId, @Valid @RequestBody MensajeDTO datos) {
        return ResponseEntity.status(HttpStatus.CREATED).body(MensajeRespuestaDTO.desde(
                mensajeService.crearMensaje(datos, procesoId, EmpresaActual.id())));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MensajeRespuestaDTO> actualizar(@PathVariable Long procesoId, @PathVariable Long id,
                                                          @Valid @RequestBody MensajeDTO datos) {
        return ResponseEntity.ok(MensajeRespuestaDTO.desde(
                mensajeService.actualizarMensaje(id, datos, procesoId, EmpresaActual.id())));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long procesoId, @PathVariable Long id) {
        mensajeService.eliminarMensaje(id, procesoId, EmpresaActual.id());
        return ResponseEntity.noContent().build();
    }
}

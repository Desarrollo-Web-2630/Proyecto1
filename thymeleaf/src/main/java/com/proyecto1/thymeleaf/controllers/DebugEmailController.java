package com.proyecto1.thymeleaf.controllers;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.proyecto1.thymeleaf.services.CorreoService;
import com.proyecto1.thymeleaf.services.VerificacionCorreoService;

// Endpoint simple para enviar correos de prueba en entorno local.

@RestController
@RequestMapping("/api/v1/debug")
@Profile({"h2", "local", "postman"})
public class DebugEmailController {

    private static final Logger log = LoggerFactory.getLogger(DebugEmailController.class);
    private final CorreoService correoService;
    private final VerificacionCorreoService verificacionCorreoService;

    public DebugEmailController(CorreoService correoService, VerificacionCorreoService verificacionCorreoService) {
        this.correoService = correoService;
        this.verificacionCorreoService = verificacionCorreoService;
    }

    @PostMapping("/email")
    public ResponseEntity<?> enviarPrueba(@RequestBody Map<String, String> body) {
        String to = body.get("to");
        String name = body.getOrDefault("name", "Usuario");
        String link = body.getOrDefault("link", "");

        if (to == null || to.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Campo 'to' es obligatorio"));
        }

        try {
            if (link == null || link.isBlank() || "ignore".equalsIgnoreCase(link)) {
                // intentar reenvío por BD; si falla, generar token debug y devolverlo en la respuesta
                try {
                    verificacionCorreoService.reenviarVerificacion(to);
                    // 202: se aceptó la solicitud de reenvío, el correo se procesa async
                    // (no se crea ni devuelve un recurso persistente, por eso no es 200/201).
                    return ResponseEntity.accepted().body(Map.of("ok", true, "to", to));
                } catch (IllegalArgumentException iae) {
                    log.warn("Usuario no encontrado para debug email, creando token debug", iae);
                    String token = verificacionCorreoService.generarTokenDebug(to);
                    String enlace = verificacionCorreoService.getBackendBaseUrl() + "/api/auth/verificar-correo?token=" + token;
                    correoService.enviarCorreoVerificacion(to, name, enlace);
                    // 201: a diferencia de las otras dos ramas (que solo disparan un envío
                    // async sin crear nada), aquí sí se genera y devuelve un recurso nuevo:
                    // el token de depuración.
                    return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("ok", true, "to", to, "debugToken", token));
                }
            } else {
                correoService.enviarCorreoVerificacion(to, name, link);
                return ResponseEntity.accepted().body(Map.of("ok", true, "to", to));
            }
        } catch (Exception e) {
            log.error("Error enviando correo de prueba", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "No se pudo enviar el correo", "detail", e.getMessage()));
        }
    }
}
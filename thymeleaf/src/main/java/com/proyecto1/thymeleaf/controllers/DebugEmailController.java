package com.proyecto1.thymeleaf.controllers;

import com.proyecto1.thymeleaf.services.CorreoService;
import com.proyecto1.thymeleaf.services.VerificacionCorreoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Endpoint simple para enviar correos de prueba en entorno local.
 *
 * Solo existe en los perfiles de desarrollo: puede generar un token de
 * verificacion para cualquier correo sin comprobar que el destinatario sea
 * quien dice ser, asi que no debe quedar expuesto fuera de ellos.
 */
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
                    return ResponseEntity.ok(Map.of("ok", true, "to", to));
                } catch (IllegalArgumentException iae) {
                    log.warn("Usuario no encontrado para debug email, creando token debug", iae);
                    String token = verificacionCorreoService.generarTokenDebug(to);
                    String enlace = verificacionCorreoService.getBackendBaseUrl() + "/api/auth/verificar-correo?token=" + token;
                    correoService.enviarCorreoVerificacion(to, name, enlace);
                    return ResponseEntity.ok(Map.of("ok", true, "to", to, "debugToken", token));
                }
            } else {
                correoService.enviarCorreoVerificacion(to, name, link);
                return ResponseEntity.ok(Map.of("ok", true, "to", to));
            }
        } catch (Exception e) {
            log.error("Error enviando correo de prueba", e);
            return ResponseEntity.status(500).body(Map.of("error", "No se pudo enviar el correo", "detail", e.getMessage()));
        }
    }
}

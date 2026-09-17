package com.proyecto1.thymeleaf.controllers;

import com.proyecto1.thymeleaf.dto.ActivarCuentaRequestDTO;
import com.proyecto1.thymeleaf.dto.VerificacionCorreoRequestDTO;
import com.proyecto1.thymeleaf.dto.VerificacionCorreoResponseDTO;
import com.proyecto1.thymeleaf.services.VerificacionCorreoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class VerificacionCorreoController {

    private final VerificacionCorreoService verificacionCorreoService;

    @GetMapping("/verificar-correo")
    public VerificacionCorreoResponseDTO verificarCorreo(@RequestParam String token) {
        return verificacionCorreoService.verificarCorreo(token);
    }

    @GetMapping("/verificar-correo/{token}")
    public VerificacionCorreoResponseDTO verificarCorreoPorPath(@PathVariable String token) {
        return verificacionCorreoService.verificarCorreo(token);
    }

    @PostMapping("/verificar-correo")
    public VerificacionCorreoResponseDTO verificarCorreoPorBody(@RequestBody(required = false) VerificacionCorreoRequestDTO request,
                                                                @RequestParam(required = false) String token) {
        String tokenFinal = token;
        if ((tokenFinal == null || tokenFinal.isBlank()) && request != null) {
            tokenFinal = request.getToken();
        }
        return verificacionCorreoService.verificarCorreo(tokenFinal);
    }

    /**
     * Activa la cuenta y fija la contrasena en un solo paso. Es el unico
     * camino de activacion para el administrador que crea POST /empresas,
     * porque a ese usuario no se le asigna ninguna contrasena utilizable al
     * registrarse (HU-01: "nunca usar contrasenas fijas").
     */
    @PostMapping("/activar-cuenta")
    public VerificacionCorreoResponseDTO activarCuenta(@Valid @RequestBody ActivarCuentaRequestDTO request) {
        return verificacionCorreoService.activarCuenta(request.getToken(), request.getNuevaPassword());
    }

    @PostMapping("/reenviar-verificacion")
    public VerificacionCorreoResponseDTO reenviarVerificacion(@RequestBody(required = false) VerificacionCorreoRequestDTO request,
                                                               @RequestParam(required = false) String correo) {
        String correoFinal = correo;
        if ((correoFinal == null || correoFinal.isBlank()) && request != null) {
            correoFinal = request.getCorreo();
        }
        if (correoFinal == null || correoFinal.isBlank()) {
            throw new IllegalArgumentException("Debe indicar el correo");
        }
        return verificacionCorreoService.reenviarVerificacion(correoFinal);
    }
}

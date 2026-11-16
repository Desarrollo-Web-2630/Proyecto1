package com.proyecto1.thymeleaf.controllers;

import com.proyecto1.thymeleaf.dto.LoginDTO;
import com.proyecto1.thymeleaf.dto.LoginRespuestaDTO;
import com.proyecto1.thymeleaf.dto.UsuarioVistaDTO;
import com.proyecto1.thymeleaf.security.JwtServicio;
import com.proyecto1.thymeleaf.security.UsuarioAutenticado;
import com.proyecto1.thymeleaf.util.EmpresaActual;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

// HU-03: inicio de sesion con JWT
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtServicio jwtServicio;

    public AuthController(AuthenticationManager authenticationManager, JwtServicio jwtServicio) {
        this.authenticationManager = authenticationManager;
        this.jwtServicio = jwtServicio;
    }

    // Credenciales malas o cuenta inactiva: GlobalExceptionHandler responde 401 con mensaje generico
    @PostMapping("/login")
    public ResponseEntity<LoginRespuestaDTO> login(@Valid @RequestBody LoginDTO login) {
        Authentication autenticacion = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(login.getCorreo().trim().toLowerCase(), login.getPassword().trim()));
        UsuarioAutenticado usuario = (UsuarioAutenticado) autenticacion.getPrincipal();
        String token = jwtServicio.generarToken(usuario);
        return ResponseEntity.ok(new LoginRespuestaDTO(token, "Bearer", jwtServicio.getExpiracionSegundos(), usuario.aVista()));
    }

    // Datos del usuario dueno del token
    @GetMapping("/yo")
    public ResponseEntity<UsuarioVistaDTO> yo() {
        return ResponseEntity.ok(EmpresaActual.usuario().aVista());
    }
}

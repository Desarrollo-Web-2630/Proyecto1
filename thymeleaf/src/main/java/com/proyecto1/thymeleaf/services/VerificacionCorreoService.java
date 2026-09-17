package com.proyecto1.thymeleaf.services;

import com.proyecto1.thymeleaf.dto.VerificacionCorreoResponseDTO;
import com.proyecto1.thymeleaf.model.Usuario;
import com.proyecto1.thymeleaf.model.VerificacionToken;
import com.proyecto1.thymeleaf.repository.UsuarioRepository;
import com.proyecto1.thymeleaf.repository.VerificacionTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class VerificacionCorreoService {

    private final UsuarioRepository usuarioRepository;
    private final VerificacionTokenRepository tokenRepository;
    private final CorreoService correoService;

    @Value("${app.frontend.base-url:http://localhost:3000}")
    private String frontendBaseUrl;

    @Value("${app.backend.base-url:http://localhost:8080}")
    private String backendBaseUrl;

    private final Duration TOKEN_TTL = Duration.ofHours(24);

    // store temporal para tokens generados en modo debug cuando la persistencia falla
    private final ConcurrentMap<String, String> debugTokens = new ConcurrentHashMap<>();

    public VerificacionCorreoService(UsuarioRepository usuarioRepository,
                                     VerificacionTokenRepository tokenRepository,
                                     CorreoService correoService) {
        this.usuarioRepository = usuarioRepository;
        this.tokenRepository = tokenRepository;
        this.correoService = correoService;
    }

    public VerificacionCorreoResponseDTO verificarCorreo(String token) {
        if (token == null || token.isBlank()) {
            return VerificacionCorreoResponseDTO.builder()
                    .verificado(false)
                    .mensaje("Token inválido")
                    .correo("")
                    .build();
        }

        return tokenRepository.findByToken(token)
                .map(t -> {
                    if (t.isUsado()) {
                        return VerificacionCorreoResponseDTO.builder()
                                .verificado(false)
                                .mensaje("Token ya utilizado")
                                .correo(t.getUsuario().getCorreo())
                                .build();
                    }

                    if (t.getExpiracion().isBefore(Instant.now())) {
                        return VerificacionCorreoResponseDTO.builder()
                                .verificado(false)
                                .mensaje("Token expirado")
                                .correo(t.getUsuario().getCorreo())
                                .build();
                    }

                    Usuario usuario = t.getUsuario();
                    usuario.setActivo(true);
                    usuarioRepository.save(usuario);

                    t.setUsado(true);
                    tokenRepository.save(t);

                    return VerificacionCorreoResponseDTO.builder()
                            .verificado(true)
                            .mensaje("Correo verificado correctamente")
                            .correo(usuario.getCorreo())
                            .build();
                })
                .orElseGet(() -> {
                    // si no está en la BD, revisar tokens temporales de debug
                    String correoDebug = debugTokens.remove(token);
                    if (correoDebug != null) {
                    return VerificacionCorreoResponseDTO.builder()
                        .verificado(true)
                        .mensaje("Correo verificado (debug)")
                        .correo(correoDebug)
                        .build();
                    }

                    return VerificacionCorreoResponseDTO.builder()
                        .verificado(false)
                        .mensaje("Token no encontrado")
                        .correo("")
                        .build();
                });
    }

    public VerificacionCorreoResponseDTO reenviarVerificacion(String correo) {
        if (correo == null || correo.isBlank()) {
            return VerificacionCorreoResponseDTO.builder()
                    .verificado(false)
                    .mensaje("Correo inválido")
                    .correo("")
                    .build();
        }

        Usuario usuario = usuarioRepository.findByCorreoIgnoreCase(correo.trim())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        String token = UUID.randomUUID().toString().replace("-", "");
        Instant expiracion = Instant.now().plus(TOKEN_TTL);

        VerificacionToken vt = new VerificacionToken(token, usuario, expiracion);
        tokenRepository.save(vt);

        // por defecto enviamos un enlace al endpoint del backend para permitir
        // verificar directamente desde el correo (útil para pruebas y Postman)
        String enlace = backendBaseUrl + "/api/auth/verificar-correo?token=" + token;
        correoService.enviarCorreoVerificacion(usuario.getCorreo(), usuario.getNombre(), enlace);

        return VerificacionCorreoResponseDTO.builder()
                .verificado(false)
                .mensaje("Se ha enviado un correo de verificación")
                .correo(usuario.getCorreo())
                .build();
    }

    // Genera un token temporal (no persistido) para pruebas/debug y lo devuelve
    public String generarTokenDebug(String correo) {
        String token = UUID.randomUUID().toString().replace("-", "");
        debugTokens.put(token, correo);
        return token;
    }

    // Exponer backendBaseUrl para usos externos (debug/controller)
    public String getBackendBaseUrl() {
        return this.backendBaseUrl;
    }
}

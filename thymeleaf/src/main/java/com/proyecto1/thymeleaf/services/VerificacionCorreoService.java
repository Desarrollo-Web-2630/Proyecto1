package com.proyecto1.thymeleaf.services;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.proyecto1.thymeleaf.dto.VerificacionCorreoResponseDTO;
import com.proyecto1.thymeleaf.model.Usuario;
import com.proyecto1.thymeleaf.model.VerificacionToken;
import com.proyecto1.thymeleaf.repository.VerificacionTokenRepository;
import com.proyecto1.thymeleaf.util.PasswordUtil;

// Verificacion de cuentas por correo (HU-01, HU-02, HU-03).

@Service
@Transactional
public class VerificacionCorreoService {

    private final UsuarioService usuarioService;
    private final VerificacionTokenRepository tokenRepository;
    private final CorreoService correoService;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.backend.base-url:http://localhost:8080}")
    private String backendBaseUrl;

    private static final Duration TOKEN_TTL = Duration.ofHours(24);

    // store temporal para tokens generados en modo debug cuando la persistencia falla
    private final ConcurrentMap<String, String> debugTokens = new ConcurrentHashMap<>();

    public VerificacionCorreoService(@Lazy UsuarioService usuarioService,
                                     VerificacionTokenRepository tokenRepository,
                                     CorreoService correoService,
                                     PasswordEncoder passwordEncoder) {
        this.usuarioService = usuarioService;
        this.tokenRepository = tokenRepository;
        this.correoService = correoService;
        this.passwordEncoder = passwordEncoder;
    }

    public void crearYEnviarToken(Usuario usuario) {
        String token = UUID.randomUUID().toString().replace("-", "");
        Instant expiracion = Instant.now().plus(TOKEN_TTL);

        VerificacionToken vt = new VerificacionToken(token, usuario, expiracion);
        tokenRepository.save(vt);

        String enlace = backendBaseUrl + "/api/auth/verificar-correo?token=" + token;
        correoService.enviarCorreoVerificacion(usuario.getCorreo(), usuario.getNombre(), enlace);
    }

    public VerificacionCorreoResponseDTO verificarCorreo(String token) {
        if (token == null || token.isBlank()) {
            return respuesta(false, "Token invalido", "");
        }

        return tokenRepository.findByToken(token)
                .map(t -> {
                    if (t.isUsado()) {
                        return respuesta(false, "Token ya utilizado", t.getUsuario().getCorreo());
                    }
                    if (t.getExpiracion().isBefore(Instant.now())) {
                        return respuesta(false, "Token expirado", t.getUsuario().getCorreo());
                    }

                    Usuario usuario = t.getUsuario();
                    usuario.setActivo(true);
                    usuarioService.guardarUsuario(usuario);

                    t.setUsado(true);
                    tokenRepository.save(t);

                    return respuesta(true, "Correo verificado correctamente", usuario.getCorreo());
                })
                .orElseGet(() -> {
                    // si no esta en la BD, revisar tokens temporales de debug
                    String correoDebug = debugTokens.remove(token);
                    if (correoDebug != null) {
                        return respuesta(true, "Correo verificado (debug)", correoDebug);
                    }
                    return respuesta(false, "Token no encontrado", "");
                });
    }

    public VerificacionCorreoResponseDTO activarCuenta(String token, String nuevaPassword) {
        if (token == null || token.isBlank()) {
            return respuesta(false, "Token invalido", "");
        }
        if (nuevaPassword == null || !PasswordUtil.cumpleReglasBasicas(nuevaPassword.trim())) {
            return respuesta(false,
                    "La contrasena debe tener al menos 8 caracteres, incluir mayuscula, minuscula y un numero", "");
        }

        return tokenRepository.findByToken(token)
                .map(t -> {
                    if (t.isUsado()) {
                        return respuesta(false, "Token ya utilizado", t.getUsuario().getCorreo());
                    }
                    if (t.getExpiracion().isBefore(Instant.now())) {
                        return respuesta(false, "Token expirado", t.getUsuario().getCorreo());
                    }

                    Usuario usuario = t.getUsuario();
                    usuario.setPassword(passwordEncoder.encode(nuevaPassword.trim()));
                    usuario.setActivo(true);
                    usuarioService.guardarUsuario(usuario);

                    t.setUsado(true);
                    tokenRepository.save(t);

                    return respuesta(true, "Cuenta activada correctamente", usuario.getCorreo());
                })
                .orElseGet(() -> respuesta(false, "Token no encontrado", ""));
    }

    static final int MAX_REENVIOS_POR_HORA = 3;

    public VerificacionCorreoResponseDTO reenviarVerificacion(String correo) {
        if (correo == null || correo.isBlank()) {
            return respuesta(false, "Correo invalido", "");
        }

        String mensajeGenerico = "Si el correo esta registrado, recibira un enlace de verificacion";

        return usuarioService.buscarPorCorreo(correo.trim())
                .map(usuario -> {
                    Instant haceUnaHoraEnVigencia = Instant.now().plus(TOKEN_TTL).minus(Duration.ofHours(1));
                    long recientes = tokenRepository.countByUsuarioIdAndExpiracionAfter(usuario.getId(), haceUnaHoraEnVigencia);
                    if (recientes >= MAX_REENVIOS_POR_HORA) {
                        // Se registra internamente pero no se le dice al cliente
                        // nada distinto: no debe poder distinguir "limite" de "no existe".
                        return respuesta(false, mensajeGenerico, "");
                    }
                    if (Boolean.TRUE.equals(usuario.getActivo())) {
                        // Ya verificado: tampoco se revela; simplemente no se envia nada.
                        return respuesta(false, mensajeGenerico, "");
                    }
                    crearYEnviarToken(usuario);
                    return respuesta(false, mensajeGenerico, "");
                })
                .orElseGet(() -> respuesta(false, mensajeGenerico, ""));
    }

    // Genera un token temporal (no persistido) para pruebas/debug y lo devuelve
    public String generarTokenDebug(String correo) {
        String token = UUID.randomUUID().toString().replace("-", "");
        debugTokens.put(token, correo);
        return token;
    }

    public String getBackendBaseUrl() {
        return this.backendBaseUrl;
    }

    private VerificacionCorreoResponseDTO respuesta(boolean verificado, String mensaje, String correo) {
        return VerificacionCorreoResponseDTO.builder()
                .verificado(verificado)
                .mensaje(mensaje)
                .correo(correo)
                .build();
    }
}

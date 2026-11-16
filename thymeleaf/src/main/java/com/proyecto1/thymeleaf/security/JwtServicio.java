package com.proyecto1.thymeleaf.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Optional;

// Genera y valida los JWT firmados con HMAC-SHA256
@Service
public class JwtServicio {

    private static final Logger log = LoggerFactory.getLogger(JwtServicio.class);
    private static final int LONGITUD_MINIMA = 32;

    private final SecretKey clave;
    private final long expiracionMinutos;

    public JwtServicio(@Value("${app.jwt.secret:}") String secreto,
                       @Value("${app.jwt.expiracion-minutos:60}") long expiracionMinutos) {
        this.clave = crearClave(secreto);
        this.expiracionMinutos = expiracionMinutos;
    }

    public String generarToken(UsuarioAutenticado usuario) {
        Instant ahora = Instant.now();
        return Jwts.builder()
                .subject(usuario.getCorreo())
                .claim("usuarioId", usuario.getId())
                .claim("empresaId", usuario.getEmpresaId())
                .claim("rol", usuario.getRolAcceso().name())
                .issuedAt(Date.from(ahora))
                .expiration(Date.from(ahora.plus(expiracionMinutos, ChronoUnit.MINUTES)))
                .signWith(clave)
                .compact();
    }

    // Devuelve el correo del token; vacio si esta vencido, alterado o mal formado
    public Optional<String> correoDelToken(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        try {
            Claims claims = Jwts.parser().verifyWith(clave).build().parseSignedClaims(token).getPayload();
            return Optional.ofNullable(claims.getSubject());
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("Token rechazado: {}", e.getMessage());
            return Optional.empty();
        }
    }

    public long getExpiracionSegundos() {
        return expiracionMinutos * 60;
    }

    private static SecretKey crearClave(String secreto) {
        if (secreto == null || secreto.isBlank()) {
            // Sin JWT_SECRET los tokens solo sirven hasta que se reinicie la app
            log.warn("JWT_SECRET no esta definida: se usa una clave temporal generada al arrancar");
            return Jwts.SIG.HS256.key().build();
        }
        byte[] bytes = secreto.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < LONGITUD_MINIMA) {
            throw new IllegalStateException("JWT_SECRET debe tener al menos " + LONGITUD_MINIMA + " caracteres");
        }
        return Keys.hmacShaKeyFor(bytes);
    }
}

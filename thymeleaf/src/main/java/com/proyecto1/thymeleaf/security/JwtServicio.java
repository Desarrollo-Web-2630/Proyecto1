package com.proyecto1.thymeleaf.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import com.proyecto1.thymeleaf.model.TokenRevocado;
import com.proyecto1.thymeleaf.repository.TokenRevocadoRepository;

@Service
public class JwtServicio {

    private static final Logger log = LoggerFactory.getLogger(JwtServicio.class);
    private static final int LONGITUD_MINIMA = 32;

    private final SecretKey clave;
    private final long expiracionMinutos;
    private final TokenRevocadoRepository revocados;

    public JwtServicio(@Value("${app.jwt.secret:}") String secreto,
                       @Value("${app.jwt.expiracion-minutos:60}") long expiracionMinutos) {
        this(secreto, expiracionMinutos, null);
    }

    @Autowired
    public JwtServicio(@Value("${app.jwt.secret:}") String secreto,
                       @Value("${app.jwt.expiracion-minutos:60}") long expiracionMinutos,
                       TokenRevocadoRepository revocados) {
        this.clave = crearClave(secreto);
        this.expiracionMinutos = expiracionMinutos;
        this.revocados = revocados;
    }

    public String generarToken(UsuarioAutenticado usuario) {
        Instant ahora = Instant.now();
        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(usuario.getCorreo())
                .claim("usuarioId", usuario.getId())
                .claim("empresaId", usuario.getEmpresaId())
                .claim("rol", usuario.getRolAcceso().name())
                .issuedAt(Date.from(ahora))
                .expiration(Date.from(ahora.plus(expiracionMinutos, ChronoUnit.MINUTES)))
                .signWith(clave)
                .compact();
    }

    public Optional<String> correoDelToken(String token) {
        Optional<Claims> claims = claims(token);
        if (claims.isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(claims.get().getSubject());
    }

    public void revocar(String token) {
        if (revocados == null) {
            return;
        }
        revocados.deleteByExpiracionBefore(Instant.now());
        Optional<Claims> claims = claims(token);
        if (claims.isEmpty()) {
            return;
        }
        Claims datos = claims.get();
        String jti = datos.getId();
        Instant expiracion = datos.getExpiration() == null
                ? Instant.now().plusSeconds(getExpiracionSegundos())
                : datos.getExpiration().toInstant();
        if (jti != null && !revocados.existsById(jti)) {
            revocados.save(new TokenRevocado(jti, expiracion));
        }
    }

    public long getExpiracionSegundos() {
        return expiracionMinutos * 60;
    }

    private Optional<Claims> claims(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        try {
            Claims datos = Jwts.parser().verifyWith(clave).build().parseSignedClaims(token).getPayload();
            if (datos.getId() != null && revocados != null && revocados.existsById(datos.getId())) {
                return Optional.empty();
            }
            return Optional.of(datos);
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("Token rechazado: {}", e.getMessage());
            return Optional.empty();
        }
    }

    private static SecretKey crearClave(String secreto) {
        if (secreto == null || secreto.isBlank()) {
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

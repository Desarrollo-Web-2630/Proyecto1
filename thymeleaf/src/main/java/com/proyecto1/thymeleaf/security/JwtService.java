package com.proyecto1.thymeleaf.security;

import com.proyecto1.thymeleaf.model.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Optional;

/**
 * Emite y valida los JWT que reemplazan a EMPRESA_ID_MOCK.
 *
 * El token lleva el id de usuario como subject, y como claims propios el id
 * de empresa y el rol de acceso: son los dos datos que antes se hardcodeaban
 * o se recalculaban en cada controlador. Con esto, cualquier endpoint puede
 * leer "la empresa del usuario autenticado" sin volver a tocar la base de
 * datos ni confiar en nada que venga del cliente en la URL o el body.
 */
@Service
public class JwtService {

    private final SecretKey clave;
    private final long expiracionMs;

    public JwtService(@Value("${app.security.jwt.secret}") String secreto,
                      @Value("${app.security.jwt.expiration-ms:3600000}") long expiracionMs) {
        if (secreto == null || secreto.isBlank()) {
            throw new IllegalStateException(
                    "app.security.jwt.secret es obligatorio: defina JWT_SECRET como variable de entorno");
        }
        // HS256 exige una clave de al menos 256 bits (32 bytes UTF-8); un
        // secreto corto por descuido rompe el arranque en vez de firmar
        // tokens debiles en silencio.
        this.clave = Keys.hmacShaKeyFor(secreto.getBytes(StandardCharsets.UTF_8));
        this.expiracionMs = expiracionMs;
    }

    public String generarToken(Usuario usuario) {
        Date ahora = new Date();
        Date expiracion = new Date(ahora.getTime() + expiracionMs);

        return Jwts.builder()
                .subject(String.valueOf(usuario.getId()))
                .claim("empresaId", usuario.getEmpresa().getId())
                .claim("rolAcceso", usuario.getRolAcceso().name())
                .claim("correo", usuario.getCorreo())
                .issuedAt(ahora)
                .expiration(expiracion)
                .signWith(clave)
                .compact();
    }

    /**
     * Devuelve los claims si el token es valido y no ha expirado; Optional
     * vacio en cualquier otro caso (firma invalida, formato invalido,
     * expirado). El filtro de autenticacion trata todos esos casos igual:
     * la peticion sigue sin autenticar, nunca revienta con un 500.
     */
    public Optional<Claims> validarYExtraerClaims(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(clave)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return Optional.of(claims);
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}

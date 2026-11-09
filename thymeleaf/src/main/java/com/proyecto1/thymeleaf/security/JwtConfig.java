package com.proyecto1.thymeleaf.security;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

// Llave y componentes para firmar (login) y validar (cada peticion) los JWT con HMAC-SHA256.
// La clave sale de app.jwt.secret (variable JWT_SECRET); nunca se escribe en el codigo.
@Configuration
public class JwtConfig {

    static final int LONGITUD_MINIMA = 32;
    private static final String ALGORITMO = "HmacSHA256";
    private static final Logger log = LoggerFactory.getLogger(JwtConfig.class);
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Bean
    public SecretKey jwtSecretKey(@Value("${app.jwt.secret:}") String secreto) {
        return crearLlave(secreto);
    }

    @Bean
    public JwtEncoder jwtEncoder(SecretKey jwtSecretKey) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(jwtSecretKey));
    }

    // Valida firma, expiracion (exp) y fecha de emision; un token alterado o vencido no pasa
    @Bean
    public JwtDecoder jwtDecoder(SecretKey jwtSecretKey) {
        return NimbusJwtDecoder.withSecretKey(jwtSecretKey).macAlgorithm(MacAlgorithm.HS256).build();
    }

    static SecretKey crearLlave(String secreto) {
        if (secreto == null || secreto.isBlank()) {
            log.warn("JWT_SECRET no esta definida: se usa una clave aleatoria. Los tokens dejan de "
                    + "ser validos cada vez que el backend se reinicia. Definala en produccion.");
            byte[] aleatoria = new byte[LONGITUD_MINIMA];
            SECURE_RANDOM.nextBytes(aleatoria);
            return new SecretKeySpec(aleatoria, ALGORITMO);
        }
        byte[] bytes = secreto.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < LONGITUD_MINIMA) {
            throw new IllegalStateException("JWT_SECRET debe tener al menos " + LONGITUD_MINIMA + " caracteres");
        }
        return new SecretKeySpec(bytes, ALGORITMO);
    }
}

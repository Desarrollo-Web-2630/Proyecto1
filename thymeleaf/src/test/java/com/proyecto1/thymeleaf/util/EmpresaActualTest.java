package com.proyecto1.thymeleaf.util;

import com.proyecto1.thymeleaf.security.TokenService;
import com.proyecto1.thymeleaf.support.ConUsuario;
import com.proyecto1.thymeleaf.support.ConUsuarioFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.lang.annotation.Annotation;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EmpresaActualTest {

    @AfterEach
    void limpiarContexto() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void id_tomaLaEmpresaDelToken() {
        SecurityContextHolder.setContext(new ConUsuarioFactory().createSecurityContext(usuario(42L, 7L)));

        assertEquals(42L, EmpresaActual.id());
        assertEquals(7L, EmpresaActual.usuarioId());
    }

    @Test
    void id_sinAutenticacion_lanzaExcepcionDeAutenticacion() {
        SecurityContextHolder.clearContext();

        assertThrows(AuthenticationCredentialsNotFoundException.class, EmpresaActual::id);
    }

    @Test
    void id_conAutenticacionQueNoEsJwt_lanzaExcepcion() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("alguien", "clave", List.of()));

        assertThrows(AuthenticationCredentialsNotFoundException.class, EmpresaActual::id);
    }

    @Test
    void id_tokenSinClaimDeEmpresa_lanzaExcepcion() {
        Jwt jwt = Jwt.withTokenValue("t").header("alg", "HS256").subject("a@a.com")
                .claim(TokenService.CLAIM_ROL, "ADMIN")
                .issuedAt(Instant.now()).expiresAt(Instant.now().plusSeconds(60)).build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));

        assertThrows(AuthenticationCredentialsNotFoundException.class, EmpresaActual::id);
    }

    private static ConUsuario usuario(long empresaId, long usuarioId) {
        return new ConUsuario() {
            @Override public long empresaId() { return empresaId; }
            @Override public long usuarioId() { return usuarioId; }
            @Override public String rol() { return "EDITOR"; }
            @Override public String correo() { return "editor@demo.com"; }
            @Override public Class<? extends Annotation> annotationType() { return ConUsuario.class; }
        };
    }
}

package com.proyecto1.thymeleaf.support;

import com.proyecto1.thymeleaf.security.TokenService;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import java.time.Instant;
import java.util.List;

// Arma el mismo tipo de autenticacion que deja Spring Security tras validar un token real
public class ConUsuarioFactory implements WithSecurityContextFactory<ConUsuario> {

    @Override
    public SecurityContext createSecurityContext(ConUsuario usuario) {
        Instant ahora = Instant.now();
        Jwt jwt = Jwt.withTokenValue("token-de-prueba")
                .header("alg", "HS256")
                .subject(usuario.correo())
                .issuedAt(ahora)
                .expiresAt(ahora.plusSeconds(3600))
                .claim(TokenService.CLAIM_USUARIO, usuario.usuarioId())
                .claim(TokenService.CLAIM_EMPRESA, usuario.empresaId())
                .claim(TokenService.CLAIM_ROL, usuario.rol())
                .build();
        SecurityContext contexto = SecurityContextHolder.createEmptyContext();
        contexto.setAuthentication(new JwtAuthenticationToken(jwt,
                List.of(new SimpleGrantedAuthority("ROLE_" + usuario.rol()))));
        return contexto;
    }
}

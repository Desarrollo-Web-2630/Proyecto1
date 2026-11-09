package com.proyecto1.thymeleaf.util;

import com.proyecto1.thymeleaf.security.TokenService;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

// Empresa y usuario de la peticion actual, tomados del JWT ya validado por Spring Security.
// Nunca se leen de un encabezado, parametro o cuerpo: el cliente no puede elegir la empresa.
public final class EmpresaActual {

    private EmpresaActual() {
    }

    public static Long id() {
        return claimNumerico(TokenService.CLAIM_EMPRESA);
    }

    public static Long usuarioId() {
        return claimNumerico(TokenService.CLAIM_USUARIO);
    }

    private static Long claimNumerico(String claim) {
        Authentication autenticacion = SecurityContextHolder.getContext().getAuthentication();
        if (autenticacion != null
                && autenticacion.getPrincipal() instanceof Jwt jwt
                && jwt.getClaims().get(claim) instanceof Number valor) {
            return valor.longValue();
        }
        throw new AuthenticationCredentialsNotFoundException("No hay un usuario autenticado en la peticion");
    }
}

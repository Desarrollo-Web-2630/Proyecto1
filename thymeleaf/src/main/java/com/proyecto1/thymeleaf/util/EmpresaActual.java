package com.proyecto1.thymeleaf.util;

import com.proyecto1.thymeleaf.security.UsuarioAutenticado;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

// Empresa y usuario de la peticion actual, tomados del token ya validado (no de algo que envie el cliente)
public final class EmpresaActual {

    private EmpresaActual() {
    }

    public static UsuarioAutenticado usuario() {
        Authentication autenticacion = SecurityContextHolder.getContext().getAuthentication();
        if (autenticacion != null && autenticacion.getPrincipal() instanceof UsuarioAutenticado usuario) {
            return usuario;
        }
        throw new AuthenticationCredentialsNotFoundException("No hay una sesion activa");
    }

    public static Long id() {
        return usuario().getEmpresaId();
    }
}

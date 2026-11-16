package com.proyecto1.thymeleaf.security;

import com.proyecto1.thymeleaf.model.Usuario;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.test.context.TestSecurityContextHolder;

// Deja un usuario autenticado en el contexto para las pruebas de controladores sin filtros
public final class SesionDePrueba {

    public static final Long USUARIO_ID = 1000L;

    private SesionDePrueba() {
    }

    public static void iniciar(Long empresaId, Usuario.RolAcceso rol) {
        UsuarioAutenticado usuario = new UsuarioAutenticado(USUARIO_ID, "prueba@demo.com", "Usuario Prueba",
                "", empresaId, rol, true);
        TestSecurityContextHolder.setAuthentication(
                new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities()));
    }

    public static void cerrar() {
        TestSecurityContextHolder.clearContext();
    }
}

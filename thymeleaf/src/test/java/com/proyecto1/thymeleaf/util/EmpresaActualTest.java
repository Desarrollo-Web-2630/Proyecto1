package com.proyecto1.thymeleaf.util;

import com.proyecto1.thymeleaf.model.Usuario;
import com.proyecto1.thymeleaf.security.SesionDePrueba;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.*;

class EmpresaActualTest {

    @AfterEach
    void limpiarContexto() {
        SesionDePrueba.cerrar();
    }

    @Test
    void id_conSesion_devuelveLaEmpresaDelToken() {
        SesionDePrueba.iniciar(42L, Usuario.RolAcceso.EDITOR);

        assertEquals(42L, EmpresaActual.id());
        assertEquals(SesionDePrueba.USUARIO_ID, EmpresaActual.usuario().getId());
    }

    @Test
    void id_sinSesion_lanzaExcepcionDeAutenticacion() {
        SecurityContextHolder.clearContext();

        assertThrows(AuthenticationCredentialsNotFoundException.class, EmpresaActual::id);
    }

    @Test
    void id_conPrincipalQueNoEsUsuarioAutenticado_lanzaExcepcion() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("anonimo", null));

        assertThrows(AuthenticationCredentialsNotFoundException.class, EmpresaActual::id);
    }
}

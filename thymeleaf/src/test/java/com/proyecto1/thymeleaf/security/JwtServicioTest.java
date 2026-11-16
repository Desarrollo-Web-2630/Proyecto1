package com.proyecto1.thymeleaf.security;

import com.proyecto1.thymeleaf.model.Usuario;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class JwtServicioTest {

    private static final String SECRETO = "clave-de-pruebas-con-mas-de-32-caracteres";

    private final UsuarioAutenticado usuario = new UsuarioAutenticado(7L, "ana@demo.com", "Ana",
            "", 3L, Usuario.RolAcceso.EDITOR, true);

    @Test
    void generarToken_yLeerlo_devuelveElCorreo() {
        JwtServicio servicio = new JwtServicio(SECRETO, 60);

        String token = servicio.generarToken(usuario);

        assertEquals(Optional.of("ana@demo.com"), servicio.correoDelToken(token));
        assertEquals(3600, servicio.getExpiracionSegundos());
    }

    @Test
    void tokenAlterado_esRechazado() {
        JwtServicio servicio = new JwtServicio(SECRETO, 60);
        String token = servicio.generarToken(usuario);
        UsuarioAutenticado otro = new UsuarioAutenticado(8L, "intruso@demo.com", "Intruso",
                "", 9L, Usuario.RolAcceso.ADMIN, true);
        String[] partes = token.split("\\.");
        String[] partesOtro = servicio.generarToken(otro).split("\\.");
        // Payload de otro usuario con la firma del token original
        String alterado = partes[0] + "." + partesOtro[1] + "." + partes[2];

        assertTrue(servicio.correoDelToken(alterado).isEmpty());
    }

    @Test
    void tokenFirmadoConOtraClave_esRechazado() {
        String token = new JwtServicio("otra-clave-distinta-tambien-de-32-caracteres", 60).generarToken(usuario);

        assertTrue(new JwtServicio(SECRETO, 60).correoDelToken(token).isEmpty());
    }

    @Test
    void tokenVencido_esRechazado() {
        JwtServicio servicio = new JwtServicio(SECRETO, -1);

        assertTrue(servicio.correoDelToken(servicio.generarToken(usuario)).isEmpty());
    }

    @Test
    void tokenVacioOBasura_esRechazado() {
        JwtServicio servicio = new JwtServicio(SECRETO, 60);

        assertTrue(servicio.correoDelToken(null).isEmpty());
        assertTrue(servicio.correoDelToken("  ").isEmpty());
        assertTrue(servicio.correoDelToken("esto.no.es-un-jwt").isEmpty());
    }

    @Test
    void secretoCorto_noArranca() {
        assertThrows(IllegalStateException.class, () -> new JwtServicio("corta", 60));
    }

    @Test
    void sinSecreto_usaClaveTemporalQueFunciona() {
        JwtServicio servicio = new JwtServicio("", 60);

        assertTrue(servicio.correoDelToken(servicio.generarToken(usuario)).isPresent());
    }

    @Test
    void usuarioAutenticado_exponeRolConPrefijoYVista() {
        assertEquals("ROLE_EDITOR", usuario.getAuthorities().iterator().next().getAuthority());
        assertEquals("ana@demo.com", usuario.getUsername());
        assertEquals(3L, usuario.aVista().getEmpresaId());
        assertTrue(usuario.isEnabled());
    }
}

package com.proyecto1.thymeleaf.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;


class ModelTest {

    @Test
    void gateway_gettersYSetters_funcionanCorrectamente() {
        Proceso proceso = new Proceso();
        proceso.setId(10L);

        Gateway gateway = new Gateway();
        gateway.setId(1L);
        gateway.setNombre("Decision de aprobacion");
        gateway.setTipo(Gateway.TipoGateway.EXCLUSIVO);
        gateway.setProceso(proceso);
        gateway.setStatus(0);

        assertEquals(1L, gateway.getId());
        assertEquals("Decision de aprobacion", gateway.getNombre());
        assertEquals(Gateway.TipoGateway.EXCLUSIVO, gateway.getTipo());
        assertEquals(proceso, gateway.getProceso());
        assertEquals(0, gateway.getStatus());
    }

    @Test
    void gateway_tipoGateway_tieneLosTresValoresEsperados() {
        assertEquals(3, Gateway.TipoGateway.values().length);
        assertNotNull(Gateway.TipoGateway.valueOf("EXCLUSIVO"));
        assertNotNull(Gateway.TipoGateway.valueOf("PARALELO"));
        assertNotNull(Gateway.TipoGateway.valueOf("INCLUSIVO"));
    }

    @Test
    void verificacionToken_constructorYGettersSetters_funcionanCorrectamente() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        Instant expiracion = Instant.now().plus(24, ChronoUnit.HOURS);

        VerificacionToken token = new VerificacionToken("abc123", usuario, expiracion);

        assertEquals("abc123", token.getToken());
        assertEquals(usuario, token.getUsuario());
        assertEquals(expiracion, token.getExpiracion());
        assertFalse(token.isUsado());

        token.setUsado(true);
        assertTrue(token.isUsado());
    }

    @Test
    void verificacionToken_constructorVacio_permiteSetearDespues() {
        VerificacionToken token = new VerificacionToken();
        token.setId(1L);
        token.setToken("xyz");

        assertEquals(1L, token.getId());
        assertEquals("xyz", token.getToken());
    }

    @Test
    void proceso_estadoProceso_tieneLosTresValoresEsperados() {
        assertEquals(3, Proceso.EstadoProceso.values().length);
        assertNotNull(Proceso.EstadoProceso.valueOf("BORRADOR"));
        assertNotNull(Proceso.EstadoProceso.valueOf("PUBLICADO"));
        assertNotNull(Proceso.EstadoProceso.valueOf("INACTIVO"));
    }

    @Test
    void usuario_rolAcceso_tieneLosTresValoresEsperados() {
        assertEquals(3, Usuario.RolAcceso.values().length);
        assertNotNull(Usuario.RolAcceso.valueOf("ADMIN"));
        assertNotNull(Usuario.RolAcceso.valueOf("EDITOR"));
        assertNotNull(Usuario.RolAcceso.valueOf("LECTURA"));
    }

    @Test
    void empresa_coleccionDeUsuarios_iniciaVaciaYAceptaElementos() {
        Empresa empresa = new Empresa();
        assertNotNull(empresa.getUsuarios());
        assertTrue(empresa.getUsuarios().isEmpty());

        Usuario usuario = new Usuario();
        empresa.getUsuarios().add(usuario);
        assertEquals(1, empresa.getUsuarios().size());
    }

    @Test
    void proceso_coleccionDeActividades_iniciaVaciaYAceptaElementos() {
        Proceso proceso = new Proceso();
        assertNotNull(proceso.getActividades());
        assertTrue(proceso.getActividades().isEmpty());
    }
}
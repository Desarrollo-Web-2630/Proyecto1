package com.proyecto1.thymeleaf.util;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.junit.jupiter.api.Assertions.*;

class EmpresaActualTest {

    @AfterEach
    void limpiarContexto() {
        // Cada test deja su propio request attributes; hay que limpiar
        // para no contaminar el siguiente test con estado global.
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void id_sinRequestAttributes_devuelveElValorPorDefecto() {
        RequestContextHolder.resetRequestAttributes();
        assertEquals(1L, EmpresaActual.id());
    }

    @Test
    void id_conEncabezadoValido_devuelveElValorDelEncabezado() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(EmpresaActual.ENCABEZADO, "42");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        assertEquals(42L, EmpresaActual.id());
    }

    @Test
    void id_sinEncabezado_devuelveElValorPorDefecto() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        assertEquals(1L, EmpresaActual.id());
    }

    @Test
    void id_conEncabezadoNoNumerico_lanzaExcepcion() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(EmpresaActual.ENCABEZADO, "no-es-un-numero");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        assertThrows(IllegalArgumentException.class, EmpresaActual::id);
    }

    @Test
    void id_conEncabezadoEnBlanco_devuelveElValorPorDefecto() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(EmpresaActual.ENCABEZADO, "   ");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        assertEquals(1L, EmpresaActual.id());
    }
}
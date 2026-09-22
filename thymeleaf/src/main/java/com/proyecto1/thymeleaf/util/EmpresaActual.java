package com.proyecto1.thymeleaf.util;

import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

// Identifica la empresa de la peticion actual mientras no hay autenticacion.

public final class EmpresaActual {

    public static final String ENCABEZADO = "X-Empresa-Id";
    private static final long POR_DEFECTO = 1L;

    private EmpresaActual() {
    }

    public static Long id() {
        RequestAttributes atributos = RequestContextHolder.getRequestAttributes();
        if (atributos instanceof ServletRequestAttributes servlet) {
            String valor = servlet.getRequest().getHeader(ENCABEZADO);
            if (valor != null && !valor.isBlank()) {
                try {
                    return Long.parseLong(valor.trim());
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("El encabezado " + ENCABEZADO + " debe ser un numero");
                }
            }
        }
        return POR_DEFECTO;
    }
}

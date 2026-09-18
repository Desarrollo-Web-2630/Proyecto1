package com.proyecto1.thymeleaf.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Punto unico para leer quien esta autenticado ahora mismo.
 *
 * Se usa desde controladores (para obtener el empresaId real y pasarselo a
 * los servicios, que ya reciben ese parametro y ya estan probados) y desde
 * los propios servicios (para verificar el rol antes de una operacion
 * sensible, como pide la especificacion: "validar autorizacion dentro de la
 * capa de servicio, no solamente en el controller"). Si no hay nadie
 * autenticado (por ejemplo con app.security.enabled=false en desarrollo)
 * lanza una excepcion clara en vez de devolver null y que el fallo aparezca
 * tres capas mas abajo como un NullPointerException.
 */
public final class ContextoSeguridad {

    private ContextoSeguridad() {
    }

    public static UsuarioPrincipal usuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof UsuarioPrincipal principal)) {
            throw new IllegalStateException(
                    "No hay un usuario autenticado en el contexto de seguridad");
        }
        return principal;
    }

    public static Long empresaIdActual() {
        return usuarioActual().empresaId();
    }

    public static boolean esAdmin() {
        return usuarioActual().rolAcceso() == com.proyecto1.thymeleaf.model.Usuario.RolAcceso.ADMIN;
    }

    /**
     * Lanza si el usuario autenticado no es ADMIN. Se llama desde los
     * servicios en las operaciones que la especificacion reserva al
     * administrador (eliminar, cambiar roles), para que la regla no dependa
     * solo de la configuracion del controlador.
     */
    public static void exigirAdmin(String accion) {
        if (!esAdmin()) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Solo un administrador puede " + accion);
        }
    }

    /**
     * Lanza si el usuario autenticado tiene rol LECTURA. ADMIN y EDITOR
     * pueden escribir; LECTURA solo puede consultar. Se usa en crear/editar
     * de procesos, actividades, arcos y gateways.
     */
    public static void exigirEscritura(String accion) {
        if (usuarioActual().rolAcceso() == com.proyecto1.thymeleaf.model.Usuario.RolAcceso.LECTURA) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Un usuario de solo lectura no puede " + accion);
        }
    }

    /**
     * Lanza si el recurso solicitado no pertenece a la empresa del usuario
     * autenticado. Es el equivalente, para Empresa, de lo que
     * findByIdAndEmpresaId ya hace en el resto de repositorios: Empresa es la
     * raiz del tenant y no tiene una columna empresa_id propia contra la que
     * filtrar, asi que la comparacion se hace aqui.
     */
    public static void exigirPropiaEmpresa(Long empresaIdDelRecurso) {
        if (!empresaIdActual().equals(empresaIdDelRecurso)) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "No tiene acceso a los datos de esa empresa");
        }
    }
}

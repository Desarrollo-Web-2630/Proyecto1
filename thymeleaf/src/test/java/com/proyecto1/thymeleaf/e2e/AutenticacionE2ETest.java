package com.proyecto1.thymeleaf.e2e;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.support.ui.ExpectedConditions;

import static org.assertj.core.api.Assertions.assertThat;

// HU-03: inicio y cierre de sesion con JWT, y proteccion de rutas en el frontend
@DisplayName("E2E - Autenticacion")
class AutenticacionE2ETest extends E2EBase {

    @Test
    @DisplayName("E2E-01 Login correcto lleva al listado de procesos de la empresa")
    void loginCorrecto_muestraLosProcesosDeLaEmpresa() {
        PaginaProcesos procesos = entrarComo(ADMIN);

        assertThat(driver.getCurrentUrl()).endsWith("/procesos");
        assertThat(procesos.usuarioActual()).contains("Admin Demo").contains("ADMIN");
        assertThat(procesos.nombres()).contains(PROCESO_DEMO);
    }

    @Test
    @DisplayName("E2E-02 Login con clave incorrecta muestra error y no entra")
    void loginConClaveIncorrecta_muestraErrorYNoEntra() {
        PaginaLogin login = new PaginaLogin(driver, espera).abrir(FRONT).escribir(ADMIN, "ClaveMala123*");

        assertThat(login.error()).isNotBlank();
        assertThat(driver.getCurrentUrl()).contains("/login");
    }

    @Test
    @DisplayName("E2E-03 Ruta protegida sin sesion redirige al login")
    void rutaProtegidaSinSesion_redirigeAlLogin() {
        driver.get(FRONT + "/procesos");

        espera.until(ExpectedConditions.urlContains("/login"));
        assertThat(driver.getCurrentUrl()).contains("/login");
    }

    @Test
    @DisplayName("E2E-04 Cerrar sesion borra el token y vuelve a bloquear las rutas")
    void cerrarSesion_bloqueaDeNuevoLasRutas() {
        entrarComo(ADMIN).cerrarSesion();

        driver.get(FRONT + "/procesos");
        espera.until(ExpectedConditions.urlContains("/login"));
        assertThat(driver.getCurrentUrl()).contains("/login");
    }
}

package com.proyecto1.thymeleaf.e2e;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.support.ui.ExpectedConditions;

import static org.assertj.core.api.Assertions.assertThat;

// Autorizacion: rol LECTURA y aislamiento de la informacion entre empresas
@DisplayName("E2E - Roles y aislamiento entre empresas")
class ControlAccesoE2ETest extends E2EBase {

    @Test
    @DisplayName("E2E-11 Usuario de lectura ve el editor en modo solo lectura y sin paleta")
    void usuarioLectura_veElEditorSoloLectura() {
        PaginaProcesos procesos = entrarComo(LECTOR);
        assertThat(procesos.usuarioActual()).contains("LECTURA");
        assertThat(procesos.puedeCrear()).isFalse();

        PaginaEditor editor = procesos.abrirEditor(PROCESO_DEMO);

        assertThat(editor.esSoloLectura()).isTrue();
        assertThat(editor.itemsDePaleta()).isZero();
    }

    @Test
    @DisplayName("E2E-12 Usuario de lectura que escribe la URL del formulario es devuelto al listado")
    void usuarioLectura_noEntraAlFormularioPorUrl() {
        entrarComo(LECTOR);

        driver.get(FRONT + "/procesos/nuevo");

        espera.until(ExpectedConditions.urlMatches(".*/procesos$"));
        assertThat(driver.getCurrentUrl()).doesNotContain("/nuevo");
    }

    @Test
    @DisplayName("E2E-13 Otra empresa solo ve sus propios procesos")
    void otraEmpresa_soloVeSusProcesos() {
        PaginaProcesos procesos = entrarComo(ADMIN_OTRA);

        assertThat(procesos.nombres()).contains(PROCESO_OTRA).doesNotContain(PROCESO_DEMO);
    }

    @Test
    @DisplayName("E2E-14 Otra empresa no puede abrir por URL el editor de un proceso ajeno")
    void otraEmpresa_noAbreElEditorDeUnProcesoAjeno() {
        Long procesoAjeno = idDeProcesoDemo();
        entrarComo(ADMIN_OTRA);

        PaginaEditor editor = new PaginaEditor(driver, espera).abrir(FRONT, procesoAjeno);

        assertThat(editor.errorDeCarga()).isNotBlank();
        assertThat(driver.getPageSource()).doesNotContain(PROCESO_DEMO);
    }
}

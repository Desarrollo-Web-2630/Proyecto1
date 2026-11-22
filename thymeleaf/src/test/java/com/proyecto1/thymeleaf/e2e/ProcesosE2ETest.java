package com.proyecto1.thymeleaf.e2e;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.support.ui.ExpectedConditions;

import static org.assertj.core.api.Assertions.assertThat;

// HU-04 a HU-07: crear, validar y buscar procesos desde la interfaz
@DisplayName("E2E - Procesos")
class ProcesosE2ETest extends E2EBase {

    @Test
    @DisplayName("E2E-05 Crear un proceso desde el formulario lo muestra en el listado")
    void crearProceso_apareceEnElListado() {
        String nombre = nombreUnico("Compra de insumos");

        entrarComo(ADMIN).nuevoProceso().llenar(nombre, "Solicitud y aprobacion de compras", "Compras").guardar();

        espera.until(ExpectedConditions.urlMatches(".*/procesos$"));
        PaginaProcesos procesos = new PaginaProcesos(driver, espera).esperarCarga().buscar(nombre);
        assertThat(procesos.nombres()).containsExactly(nombre);
    }

    @Test
    @DisplayName("E2E-06 El formulario vacio muestra las validaciones y no guarda")
    void formularioVacio_muestraValidacionesYNoGuarda() {
        PaginaFormularioProceso formulario = entrarComo(ADMIN).nuevoProceso();

        formulario.guardar();

        assertThat(formulario.erroresDeCampo())
                .contains("El nombre es obligatorio", "La descripción es obligatoria", "La categoría es obligatoria");
        assertThat(driver.getCurrentUrl()).endsWith("/procesos/nuevo");
    }

    @Test
    @DisplayName("E2E-07 Un nombre repetido muestra el error de negocio del backend")
    void nombreRepetido_muestraElErrorDelBackend() {
        PaginaFormularioProceso formulario = entrarComo(ADMIN).nuevoProceso();

        formulario.llenar(PROCESO_DEMO, "Duplicado a proposito", "RRHH").guardar();

        assertThat(formulario.errorDelServidor()).contains("Ya existe un proceso llamado");
        assertThat(driver.getCurrentUrl()).endsWith("/procesos/nuevo");
    }

    @Test
    @DisplayName("E2E-08 Buscar por nombre filtra el listado paginado")
    void buscarPorNombre_filtraElListado() {
        PaginaProcesos procesos = entrarComo(ADMIN).buscar("vacaciones");

        assertThat(procesos.nombres()).isNotEmpty().allMatch(n -> n.toLowerCase().contains("vacaciones"));
    }
}

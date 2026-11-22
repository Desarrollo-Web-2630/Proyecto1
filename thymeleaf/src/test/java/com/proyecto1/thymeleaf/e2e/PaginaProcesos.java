package com.proyecto1.thymeleaf.e2e;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;

/** Page Object del listado /procesos y del encabezado de la aplicacion. */
class PaginaProcesos {

    static final By FILA = By.cssSelector("[data-testid=fila-proceso]");
    static final By NOMBRE = By.cssSelector("[data-testid=nombre-proceso]");
    static final By NUEVO = By.cssSelector("[data-testid=nuevo-proceso]");
    static final By BUSCAR_NOMBRE = By.cssSelector("[data-testid=buscar-nombre]");
    static final By BUSCAR = By.cssSelector("[data-testid=buscar]");
    static final By ABRIR_EDITOR = By.cssSelector("[data-testid=abrir-editor]");
    static final By USUARIO = By.cssSelector("[data-testid=usuario-actual]");
    static final By CERRAR_SESION = By.cssSelector("[data-testid=cerrar-sesion]");

    private final WebDriver driver;
    private final WebDriverWait espera;

    PaginaProcesos(WebDriver driver, WebDriverWait espera) {
        this.driver = driver;
        this.espera = espera;
    }

    PaginaProcesos esperarCarga() {
        espera.until(ExpectedConditions.urlContains("/procesos"));
        espera.until(d -> !d.findElements(FILA).isEmpty() || d.getPageSource().contains("No hay procesos registrados"));
        return this;
    }

    List<String> nombres() {
        return driver.findElements(NOMBRE).stream().map(WebElement::getText).toList();
    }

    String usuarioActual() {
        return espera.until(ExpectedConditions.visibilityOfElementLocated(USUARIO)).getText();
    }

    boolean puedeCrear() {
        return !driver.findElements(NUEVO).isEmpty();
    }

    PaginaFormularioProceso nuevoProceso() {
        espera.until(ExpectedConditions.elementToBeClickable(NUEVO)).click();
        return new PaginaFormularioProceso(driver, espera).esperarCarga();
    }

    PaginaProcesos buscar(String nombre) {
        WebElement campo = driver.findElement(BUSCAR_NOMBRE);
        campo.clear();
        campo.sendKeys(nombre);
        driver.findElement(BUSCAR).click();
        // Mientras carga la tabla desaparece: se espera a que haya filas y todas coincidan
        String buscado = nombre.toLowerCase();
        espera.until(d -> {
            List<WebElement> nombres = d.findElements(NOMBRE);
            return !nombres.isEmpty() && nombres.stream().allMatch(e -> e.getText().toLowerCase().contains(buscado));
        });
        return this;
    }

    PaginaEditor abrirEditor(String nombreProceso) {
        WebElement fila = espera.until(d -> d.findElements(FILA).stream()
                .filter(f -> f.findElement(NOMBRE).getText().equals(nombreProceso))
                .findFirst().orElse(null));
        fila.findElement(ABRIR_EDITOR).click();
        return new PaginaEditor(driver, espera).esperarCarga();
    }

    void cerrarSesion() {
        espera.until(ExpectedConditions.elementToBeClickable(CERRAR_SESION)).click();
        espera.until(ExpectedConditions.urlContains("/login"));
    }
}

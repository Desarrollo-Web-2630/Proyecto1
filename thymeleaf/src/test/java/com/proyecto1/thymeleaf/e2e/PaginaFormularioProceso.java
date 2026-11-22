package com.proyecto1.thymeleaf.e2e;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;

/** Page Object de /procesos/nuevo. */
class PaginaFormularioProceso {

    static final By NOMBRE = By.cssSelector("[data-testid=proceso-nombre]");
    static final By DESCRIPCION = By.cssSelector("[data-testid=proceso-descripcion]");
    static final By CATEGORIA = By.cssSelector("[data-testid=proceso-categoria]");
    static final By GUARDAR = By.cssSelector("[data-testid=proceso-guardar]");
    static final By ERROR_SERVIDOR = By.cssSelector("[data-testid=proceso-error]");
    static final By ERROR_CAMPO = By.cssSelector("small.error");

    private final WebDriver driver;
    private final WebDriverWait espera;

    PaginaFormularioProceso(WebDriver driver, WebDriverWait espera) {
        this.driver = driver;
        this.espera = espera;
    }

    PaginaFormularioProceso esperarCarga() {
        espera.until(ExpectedConditions.visibilityOfElementLocated(NOMBRE));
        return this;
    }

    PaginaFormularioProceso llenar(String nombre, String descripcion, String categoria) {
        driver.findElement(NOMBRE).sendKeys(nombre);
        driver.findElement(DESCRIPCION).sendKeys(descripcion);
        driver.findElement(CATEGORIA).sendKeys(categoria);
        return this;
    }

    void guardar() {
        driver.findElement(GUARDAR).click();
    }

    List<String> erroresDeCampo() {
        espera.until(ExpectedConditions.visibilityOfElementLocated(ERROR_CAMPO));
        return driver.findElements(ERROR_CAMPO).stream().map(WebElement::getText).toList();
    }

    String errorDelServidor() {
        return espera.until(ExpectedConditions.visibilityOfElementLocated(ERROR_SERVIDOR)).getText();
    }
}

package com.proyecto1.thymeleaf.e2e;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/** Page Object de /login. */
class PaginaLogin {

    static final By CORREO = By.cssSelector("[data-testid=login-correo]");
    static final By PASSWORD = By.cssSelector("[data-testid=login-password]");
    static final By ENTRAR = By.cssSelector("[data-testid=login-entrar]");
    static final By ERROR = By.cssSelector("[data-testid=login-error]");

    private final WebDriver driver;
    private final WebDriverWait espera;

    PaginaLogin(WebDriver driver, WebDriverWait espera) {
        this.driver = driver;
        this.espera = espera;
    }

    PaginaLogin abrir(String front) {
        driver.get(front + "/login");
        espera.until(ExpectedConditions.visibilityOfElementLocated(CORREO));
        return this;
    }

    PaginaLogin escribir(String correo, String clave) {
        driver.findElement(CORREO).clear();
        driver.findElement(CORREO).sendKeys(correo);
        driver.findElement(PASSWORD).clear();
        driver.findElement(PASSWORD).sendKeys(clave);
        driver.findElement(ENTRAR).click();
        return this;
    }

    PaginaProcesos entrar(String correo, String clave) {
        escribir(correo, clave);
        return new PaginaProcesos(driver, espera).esperarCarga();
    }

    String error() {
        return espera.until(ExpectedConditions.visibilityOfElementLocated(ERROR)).getText();
    }
}

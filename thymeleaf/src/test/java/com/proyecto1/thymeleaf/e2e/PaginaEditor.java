package com.proyecto1.thymeleaf.e2e;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;

/** Page Object del editor grafico /procesos/:id/editor (paleta, lienzo y dialogos). */
class PaginaEditor {

    static final By TITULO = By.cssSelector("[data-testid=editor-titulo]");
    static final By ERROR_CARGA = By.cssSelector("[data-testid=editor-error-carga]");
    static final By ERROR = By.cssSelector("[data-testid=editor-error]");
    static final By SOLO_LECTURA = By.cssSelector("[data-testid=solo-lectura]");
    static final By ITEM_PALETA = By.cssSelector("[data-testid^=paleta-]");
    static final By TABLERO = By.cssSelector("[data-testid=tablero]");
    static final By POOL = By.cssSelector("[data-testid=pool-propietario]");
    static final By LANE = By.cssSelector("[data-testid=lane]");
    static final By ACTIVIDAD = By.cssSelector("[data-testid=actividad]");
    static final By ARCO = By.cssSelector("[data-testid=arco]");
    static final By CONECTOR = By.cssSelector("i.conector");
    static final By DIALOGO = By.cssSelector("[data-testid=dialogo]");
    static final By DIALOGO_NOMBRE = By.cssSelector("[data-testid=dialogo-nombre]");
    static final By DIALOGO_GUARDAR = By.cssSelector("[data-testid=dialogo-guardar]");

    // Mismos valores que el lienzo de Angular (lienzo-proceso.component.ts)
    private static final int DESPLAZAMIENTO_X = 180;
    private static final int ANCHO_ACTIVIDAD = 140;

    private final WebDriver driver;
    private final WebDriverWait espera;

    PaginaEditor(WebDriver driver, WebDriverWait espera) {
        this.driver = driver;
        this.espera = espera;
    }

    PaginaEditor esperarCarga() {
        espera.until(ExpectedConditions.or(
                ExpectedConditions.visibilityOfElementLocated(TITULO),
                ExpectedConditions.visibilityOfElementLocated(ERROR_CARGA)));
        return this;
    }

    PaginaEditor abrir(String front, Long procesoId) {
        driver.get(front + "/procesos/" + procesoId + "/editor");
        return esperarCarga();
    }

    PaginaEditor recargar() {
        driver.navigate().refresh();
        return esperarCarga();
    }

    String titulo() {
        return driver.findElement(TITULO).getText();
    }

    String errorDeCarga() {
        return espera.until(ExpectedConditions.visibilityOfElementLocated(ERROR_CARGA)).getText();
    }

    boolean esSoloLectura() {
        return !driver.findElements(SOLO_LECTURA).isEmpty();
    }

    int itemsDePaleta() {
        return driver.findElements(ITEM_PALETA).size();
    }

    // ---- Construccion del diagrama arrastrando desde la paleta ----

    PaginaEditor agregarPool(String nombre) {
        soltarDePaleta("POOL_PROPIETARIO", 300, 60);
        completarDialogo(nombre);
        espera.until(ExpectedConditions.textToBePresentInElementLocated(POOL, nombre));
        return this;
    }

    PaginaEditor agregarLane(String nombre) {
        int antes = driver.findElements(LANE).size();
        soltarDePaleta("LANE", 300, 60);
        completarDialogo(nombre);
        espera.until(d -> d.findElements(LANE).size() == antes + 1);
        return this;
    }

    /** Suelta una actividad dentro de la lane indicada, con su borde izquierdo en {@code x} (coordenada del modelo). */
    PaginaEditor agregarActividad(String nombre, int indiceLane, int x) {
        WebElement lane = driver.findElements(LANE).get(indiceLane);
        double[] caja = ArrastrarYSoltar.cajaRelativa(driver, lane, tablero());
        double dx = DESPLAZAMIENTO_X + x + ANCHO_ACTIVIDAD / 2.0;
        double dy = caja[1] + caja[3] / 2;
        ArrastrarYSoltar.ejecutar(driver, itemPaleta("ACTIVIDAD"), tablero(), dx, dy);
        completarDialogo(nombre);
        actividad(nombre);
        return this;
    }

    // ---- Interaccion con elementos existentes ----

    WebElement actividad(String nombre) {
        return espera.until(d -> d.findElements(ACTIVIDAD).stream()
                .filter(a -> a.getText().contains(nombre))
                .findFirst().orElse(null));
    }

    List<String> actividades() {
        return driver.findElements(ACTIVIDAD).stream().map(WebElement::getText).toList();
    }

    /** Posicion de la actividad en el lienzo, relativa al tablero: [x, y]. */
    double[] posicion(String nombre) {
        double[] caja = ArrastrarYSoltar.cajaRelativa(driver, actividad(nombre), tablero());
        return new double[] {caja[0], caja[1]};
    }

    /** Arrastra la actividad dentro de su lane y la suelta desplazada (deltaX, deltaY) pixeles. */
    PaginaEditor moverActividad(String nombre, int deltaX, int deltaY) {
        WebElement actividad = actividad(nombre);
        double[] caja = ArrastrarYSoltar.cajaRelativa(driver, actividad, tablero());
        double dx = caja[0] + caja[2] / 2 + deltaX;
        double dy = caja[1] + caja[3] / 2 + deltaY;
        ArrastrarYSoltar.ejecutar(driver, actividad, tablero(), dx, dy);
        return this;
    }

    /** Arrastra el punto conector de {@code origen} hasta el centro de {@code destino}. */
    PaginaEditor conectar(String origen, String destino) {
        int antes = arcos();
        WebElement conector = actividad(origen).findElement(CONECTOR);
        double[] caja = ArrastrarYSoltar.cajaRelativa(driver, actividad(destino), tablero());
        ArrastrarYSoltar.ejecutar(driver, conector, tablero(), caja[0] + caja[2] / 2, caja[1] + caja[3] / 2);
        espera.until(d -> arcos() == antes + 1);
        return this;
    }

    int arcos() {
        return driver.findElements(ARCO).size();
    }

    int lanes() {
        return driver.findElements(LANE).size();
    }

    boolean tienePool() {
        return !driver.findElements(POOL).isEmpty();
    }

    String error() {
        List<WebElement> errores = driver.findElements(ERROR);
        return errores.isEmpty() ? "" : errores.get(0).getText();
    }

    // ---- Auxiliares ----

    private void soltarDePaleta(String clave, double dx, double dy) {
        ArrastrarYSoltar.ejecutar(driver, itemPaleta(clave), tablero(), dx, dy);
    }

    private void completarDialogo(String nombre) {
        WebElement campo = espera.until(ExpectedConditions.visibilityOfElementLocated(DIALOGO_NOMBRE));
        campo.clear();
        campo.sendKeys(nombre);
        driver.findElement(DIALOGO_GUARDAR).click();
        espera.until(ExpectedConditions.invisibilityOfElementLocated(DIALOGO));
    }

    private WebElement itemPaleta(String clave) {
        return espera.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("[data-testid=paleta-" + clave + "]")));
    }

    private WebElement tablero() {
        return espera.until(ExpectedConditions.presenceOfElementLocated(TABLERO));
    }
}

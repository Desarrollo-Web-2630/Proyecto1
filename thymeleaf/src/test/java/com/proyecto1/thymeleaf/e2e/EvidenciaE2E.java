package com.proyecto1.thymeleaf.e2e;

import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Guarda una captura de pantalla al terminar cada prueba E2E (antes de cerrar el
 * navegador) en target/e2e/capturas. Si la prueba fallo, el archivo empieza por FALLO-.
 */
class EvidenciaE2E implements AfterTestExecutionCallback {

    private static final Path CARPETA = Path.of("target", "e2e", "capturas");

    /** Captura intermedia dentro de una prueba (por ejemplo, el antes de un drag and drop). */
    static void capturar(WebDriver driver, String nombre) {
        if (!(driver instanceof TakesScreenshot camara)) {
            return;
        }
        try {
            Files.createDirectories(CARPETA);
            Files.write(CARPETA.resolve(nombre + ".png"), camara.getScreenshotAs(OutputType.BYTES));
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo guardar la captura " + nombre, e);
        }
    }

    @Override
    public void afterTestExecution(ExtensionContext contexto) throws IOException {
        Object prueba = contexto.getRequiredTestInstance();
        if (!(prueba instanceof E2EBase base) || !(base.driver instanceof TakesScreenshot camara)) {
            return;
        }
        boolean fallo = contexto.getExecutionException().isPresent();
        String nombre = (fallo ? "FALLO-" : "") + contexto.getRequiredTestClass().getSimpleName()
                + "-" + contexto.getRequiredTestMethod().getName() + ".png";
        Files.createDirectories(CARPETA);
        Files.write(CARPETA.resolve(nombre), camara.getScreenshotAs(OutputType.BYTES));
    }
}

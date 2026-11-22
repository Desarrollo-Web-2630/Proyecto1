package com.proyecto1.thymeleaf.e2e;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

/**
 * Drag and drop nativo de HTML5 (atributo draggable + eventos dragstart/dragover/drop).
 *
 * Las Actions de Selenium mueven el mouse, pero Chrome no dispara los eventos de
 * arrastre HTML5 con ellas, asi que se lanzan los mismos eventos que generaria el
 * navegador, con un DataTransfer compartido y coordenadas reales (clientX/clientY).
 */
final class ArrastrarYSoltar {

    // Entre dragstart y drop se deja correr un ciclo de deteccion de cambios de Angular
    // (como pasa con un arrastre real), porque el lienzo recibe el item de la paleta por un input.
    private static final String SCRIPT = """
            const [origen, destino, dx, dy, listo] = arguments;
            const datos = new DataTransfer();
            const caja = origen.getBoundingClientRect();
            const lanzar = (tipo, elemento, cx, cy) => elemento.dispatchEvent(new DragEvent(tipo, {
              bubbles: true, cancelable: true, composed: true, clientX: cx, clientY: cy, dataTransfer: datos
            }));
            lanzar('dragstart', origen, caja.left + caja.width / 2, caja.top + caja.height / 2);
            setTimeout(() => {
              const meta = destino.getBoundingClientRect();
              const x = meta.left + dx;
              const y = meta.top + dy;
              lanzar('dragenter', destino, x, y);
              lanzar('dragover', destino, x, y);
              setTimeout(() => {
                lanzar('drop', destino, x, y);
                lanzar('dragend', origen, x, y);
                listo();
              }, 100);
            }, 150);
            """;

    private ArrastrarYSoltar() {
    }

    /**
     * Arrastra {@code origen} (tomandolo por su centro) y lo suelta sobre {@code destino}
     * en el punto (dx, dy) medido desde la esquina superior izquierda del destino.
     */
    static void ejecutar(WebDriver driver, WebElement origen, WebElement destino, double dx, double dy) {
        ((JavascriptExecutor) driver).executeAsyncScript(SCRIPT, origen, destino, dx, dy);
    }

    /** Posicion de {@code elemento} respecto a {@code referencia}: [izquierda, arriba, ancho, alto]. */
    static double[] cajaRelativa(WebDriver driver, WebElement elemento, WebElement referencia) {
        @SuppressWarnings("unchecked")
        List<Number> valores = (List<Number>) ((JavascriptExecutor) driver).executeScript("""
                const a = arguments[0].getBoundingClientRect();
                const b = arguments[1].getBoundingClientRect();
                return [a.left - b.left, a.top - b.top, a.width, a.height];
                """, elemento, referencia);
        return valores.stream().mapToDouble(Number::doubleValue).toArray();
    }
}

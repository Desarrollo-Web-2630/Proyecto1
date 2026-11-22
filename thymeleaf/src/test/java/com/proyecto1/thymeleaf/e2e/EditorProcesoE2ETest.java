package com.proyecto1.thymeleaf.e2e;

import com.proyecto1.thymeleaf.model.Actividad;
import com.proyecto1.thymeleaf.model.Proceso;
import com.proyecto1.thymeleaf.repository.ActividadRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

// HU de modelado: pool, lanes, actividades y arcos con drag and drop sobre el lienzo
@DisplayName("E2E - Editor de procesos (drag and drop)")
class EditorProcesoE2ETest extends E2EBase {

    @Autowired private ActividadRepository actividadRepository;

    @Test
    @DisplayName("E2E-09 Modelar un proceso completo arrastrando desde la paleta y conectar actividades")
    void modelarProceso_conPoolLaneActividadesYArco() {
        Proceso proceso = procesoVacioDeDemo("Modelado E2E");

        PaginaEditor editor = entrarComo(ADMIN).abrirEditor(proceso.getNombre());
        assertThat(editor.titulo()).isEqualTo(proceso.getNombre());

        editor.agregarPool("Empresa Demo")
                .agregarLane("Solicitante")
                .agregarActividad("Registrar solicitud", 0, 40)
                .agregarActividad("Revisar solicitud", 0, 320)
                .conectar("Registrar solicitud", "Revisar solicitud");

        // Al recargar, todo viene del backend: el diagrama quedo guardado
        editor.recargar();
        assertThat(editor.tienePool()).isTrue();
        assertThat(editor.lanes()).isEqualTo(1);
        assertThat(editor.actividades()).containsExactlyInAnyOrder("Registrar solicitud", "Revisar solicitud");
        assertThat(editor.arcos()).isEqualTo(1);
        assertThat(editor.error()).isEmpty();
    }

    @Test
    @DisplayName("E2E-10 Mover una actividad con drag and drop guarda la nueva posicion")
    void moverActividad_guardaLaNuevaPosicion() {
        Proceso proceso = procesoVacioDeDemo("Mover E2E");
        PaginaEditor editor = entrarComo(ADMIN).abrirEditor(proceso.getNombre())
                .agregarPool("Empresa Demo")
                .agregarLane("Aprobador")
                .agregarActividad("Aprobar", 0, 40);
        double[] antes = editor.posicion("Aprobar");
        Long actividadId = actividadRepository.findByProcesoId(proceso.getId()).get(0).getId();
        int xGuardadaAntes = posicionXEnBaseDeDatos(actividadId);

        editor.moverActividad("Aprobar", 200, 0);

        // La base de datos recibe la nueva posicion (POST /actividades/{id}/mover)
        espera.until(d -> posicionXEnBaseDeDatos(actividadId) == xGuardadaAntes + 200);
        // Y al recargar la pagina la actividad aparece en el nuevo lugar
        editor.recargar();
        double[] despues = editor.posicion("Aprobar");
        assertThat(despues[0] - antes[0]).isEqualTo(200.0);
        assertThat(despues[1]).isEqualTo(antes[1]);
    }

    private int posicionXEnBaseDeDatos(Long actividadId) {
        Actividad actividad = actividadRepository.findById(actividadId).orElseThrow();
        return actividad.getPosicionX();
    }
}

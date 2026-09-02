package com.proyecto1.thymeleaf.services;

import com.proyecto1.thymeleaf.model.Actividad;
import com.proyecto1.thymeleaf.model.Empresa;
import com.proyecto1.thymeleaf.model.Proceso;
import com.proyecto1.thymeleaf.repository.EmpresaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifica los criterios de aceptacion de la HU-04 (crear proceso) y la
 * HU-08 (crear actividad).
 */
@SpringBootTest
@Transactional
class ProcesoActividadServiceTest {

    @Autowired
    private ProcesoService procesoService;

    @Autowired
    private ActividadService actividadService;

    @Autowired
    private EmpresaRepository empresaRepository;

    private Long empresaA;
    private Long empresaB;

    @BeforeEach
    void prepararEmpresas() {
        empresaA = crearEmpresa("Empresa A", "900111222");
        empresaB = crearEmpresa("Empresa B", "900333444");
    }

    // ---------- HU-04 - Crear proceso ----------

    @Test
    void elProcesoSeRegistraConSusDatosYQuedaAsociadoALaEmpresa() {
        Proceso guardado = procesoService.crearProceso(nuevoProceso("Compras"), empresaA);

        assertEquals("Compras", guardado.getNombre());
        assertEquals("Proceso de prueba", guardado.getDescripcion());
        assertEquals("Operaciones", guardado.getCategoria());
        assertEquals(empresaA, guardado.getEmpresa().getId());
    }

    @Test
    void elProcesoNaceEnBorradorAunqueLleguePublicadoDelFormulario() {
        Proceso entrada = nuevoProceso("Compras");
        entrada.setEstado(Proceso.EstadoProceso.PUBLICADO);

        Proceso guardado = procesoService.crearProceso(entrada, empresaA);

        assertEquals(Proceso.EstadoProceso.BORRADOR, guardado.getEstado());
    }

    @Test
    void elProcesoPuedePasarAPublicado() {
        Proceso guardado = procesoService.crearProceso(nuevoProceso("Compras"), empresaA);

        Proceso publicado = procesoService.publicarProceso(guardado.getId(), empresaA);

        assertEquals(Proceso.EstadoProceso.PUBLICADO, publicado.getEstado());
    }

    @Test
    void elNombreDelProcesoEsUnicoDentroDeLaEmpresa() {
        procesoService.crearProceso(nuevoProceso("Compras"), empresaA);

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> procesoService.crearProceso(nuevoProceso("Compras"), empresaA));

        assertTrue(error.getMessage().contains("Ya existe un proceso"));
    }

    @Test
    void otraEmpresaSiPuedeUsarElMismoNombreDeProceso() {
        procesoService.crearProceso(nuevoProceso("Compras"), empresaA);

        Proceso deLaOtraEmpresa = procesoService.crearProceso(nuevoProceso("Compras"), empresaB);

        assertEquals(empresaB, deLaOtraEmpresa.getEmpresa().getId());
    }

    @Test
    void unaEmpresaNoAlcanzaLosProcesosDeOtra() {
        Proceso deA = procesoService.crearProceso(nuevoProceso("Compras"), empresaA);

        assertThrows(IllegalArgumentException.class,
                () -> procesoService.obtenerPorIdYEmpresa(deA.getId(), empresaB));
    }

    @Test
    void elNombreDelProcesoEsObligatorio() {
        Proceso sinNombre = nuevoProceso("   ");

        assertThrows(IllegalArgumentException.class,
                () -> procesoService.crearProceso(sinNombre, empresaA));
    }

    // ---------- HU-08 - Crear actividad ----------

    @Test
    void laActividadQuedaAsociadaAlProcesoALaLaneYALaPosicionIndicada() {
        Proceso proceso = procesoService.crearProceso(nuevoProceso("Compras"), empresaA);

        Actividad guardada = actividadService.crearActividad(
                nuevaActividad("Revisar solicitud", 120, 45, 7L), proceso.getId(), empresaA);

        assertEquals("Revisar solicitud", guardada.getNombre());
        assertEquals("TAREA_USUARIO", guardada.getTipoActividad());
        assertEquals(proceso.getId(), guardada.getProceso().getId());
        assertEquals(7L, guardada.getLaneId());
        assertEquals(120, guardada.getPosicionX());
        assertEquals(45, guardada.getPosicionY());
    }

    @Test
    void elNombreDeLaActividadEsUnicoDentroDelProceso() {
        Proceso proceso = procesoService.crearProceso(nuevoProceso("Compras"), empresaA);
        actividadService.crearActividad(
                nuevaActividad("Revisar solicitud", 10, 10, 1L), proceso.getId(), empresaA);

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> actividadService.crearActividad(
                        nuevaActividad("Revisar solicitud", 80, 80, 2L), proceso.getId(), empresaA));

        assertTrue(error.getMessage().contains("Ya existe una actividad"));
    }

    @Test
    void otroProcesoSiPuedeUsarElMismoNombreDeActividad() {
        Proceso compras = procesoService.crearProceso(nuevoProceso("Compras"), empresaA);
        Proceso ventas = procesoService.crearProceso(nuevoProceso("Ventas"), empresaA);
        actividadService.crearActividad(
                nuevaActividad("Revisar solicitud", 10, 10, 1L), compras.getId(), empresaA);

        Actividad enVentas = actividadService.crearActividad(
                nuevaActividad("Revisar solicitud", 10, 10, 1L), ventas.getId(), empresaA);

        assertEquals(ventas.getId(), enVentas.getProceso().getId());
    }

    @Test
    void noSePuedenAgregarActividadesAlProcesoDeOtraEmpresa() {
        Proceso deA = procesoService.crearProceso(nuevoProceso("Compras"), empresaA);

        assertThrows(IllegalArgumentException.class,
                () -> actividadService.crearActividad(
                        nuevaActividad("Revisar solicitud", 10, 10, 1L), deA.getId(), empresaB));
    }

    @Test
    void laActividadExigeNombreTipoLaneYPosicion() {
        Proceso proceso = procesoService.crearProceso(nuevoProceso("Compras"), empresaA);
        Long procesoId = proceso.getId();

        Actividad sinNombre = nuevaActividad("  ", 10, 10, 1L);
        Actividad sinTipo = nuevaActividad("A", 10, 10, 1L);
        sinTipo.setTipoActividad(null);
        Actividad sinLane = nuevaActividad("B", 10, 10, null);
        Actividad sinPosicion = nuevaActividad("C", null, 10, 1L);

        assertThrows(IllegalArgumentException.class,
                () -> actividadService.crearActividad(sinNombre, procesoId, empresaA));
        assertThrows(IllegalArgumentException.class,
                () -> actividadService.crearActividad(sinTipo, procesoId, empresaA));
        assertThrows(IllegalArgumentException.class,
                () -> actividadService.crearActividad(sinLane, procesoId, empresaA));
        assertThrows(IllegalArgumentException.class,
                () -> actividadService.crearActividad(sinPosicion, procesoId, empresaA));
    }

    @Test
    void moverActividadActualizaSoloLaPosicion() {
        Proceso proceso = procesoService.crearProceso(nuevoProceso("Compras"), empresaA);
        Actividad actividad = actividadService.crearActividad(
                nuevaActividad("Revisar solicitud", 10, 10, 1L), proceso.getId(), empresaA);

        Actividad movida = actividadService.moverActividad(actividad.getId(), 300, 240, empresaA);

        assertEquals(300, movida.getPosicionX());
        assertEquals(240, movida.getPosicionY());
        assertEquals("Revisar solicitud", movida.getNombre());
    }

    // ---------- utilidades ----------

    private Long crearEmpresa(String nombre, String nit) {
        Empresa empresa = new Empresa();
        empresa.setNombre(nombre);
        empresa.setNit(nit);
        empresa.setCorreo("contacto@" + nit + ".com");
        return empresaRepository.save(empresa).getId();
    }

    private Proceso nuevoProceso(String nombre) {
        Proceso proceso = new Proceso();
        proceso.setNombre(nombre);
        proceso.setDescripcion("Proceso de prueba");
        proceso.setCategoria("Operaciones");
        return proceso;
    }

    private Actividad nuevaActividad(String nombre, Integer x, Integer y, Long laneId) {
        Actividad actividad = new Actividad();
        actividad.setNombre(nombre);
        actividad.setTipoActividad("TAREA_USUARIO");
        actividad.setPosicionX(x);
        actividad.setPosicionY(y);
        actividad.setLaneId(laneId);
        return actividad;
    }
}

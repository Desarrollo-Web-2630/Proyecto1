package com.proyecto1.thymeleaf.services;

import com.proyecto1.thymeleaf.dto.ActividadDTO;
import com.proyecto1.thymeleaf.dto.EmpresaDTO;
import com.proyecto1.thymeleaf.dto.ProcesoDTO;
import com.proyecto1.thymeleaf.model.Actividad;
import com.proyecto1.thymeleaf.model.Proceso;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifica los criterios de aceptacion de la HU-04 (crear proceso) y las
 * HU-08 / HU-09 / HU-10 (crear, editar y eliminar actividad).
 */
@SpringBootTest
@Transactional
class ProcesoActividadServiceTest {

    @Autowired
    private ProcesoService procesoService;

    @Autowired
    private ActividadService actividadService;

    @Autowired
    private EmpresaService empresaService;

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
    void elProcesoNaceSiempreEnBorrador() {
        // ProcesoDTO ni siquiera tiene campo estado: el formulario no puede fijarlo
        Proceso guardado = procesoService.crearProceso(nuevoProceso("Compras"), empresaA);

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
        ProcesoDTO sinNombre = nuevoProceso("   ");

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

        ActividadDTO sinNombre = nuevaActividad("  ", 10, 10, 1L);
        ActividadDTO sinTipo = nuevaActividad("A", 10, 10, 1L);
        sinTipo.setTipoActividad(null);
        ActividadDTO sinLane = nuevaActividad("B", 10, 10, null);
        ActividadDTO sinPosicion = nuevaActividad("C", null, 10, 1L);

        assertThrows(IllegalArgumentException.class,
                () -> actividadService.crearActividad(sinNombre, procesoId, empresaA));
        assertThrows(IllegalArgumentException.class,
                () -> actividadService.crearActividad(sinTipo, procesoId, empresaA));
        assertThrows(IllegalArgumentException.class,
                () -> actividadService.crearActividad(sinLane, procesoId, empresaA));
        assertThrows(IllegalArgumentException.class,
                () -> actividadService.crearActividad(sinPosicion, procesoId, empresaA));
    }

    // ---------- HU-09 - Editar actividad ----------

    @Test
    void editarLaActividadCambiaNombreTipoYLaneResponsable() {
        Proceso proceso = procesoService.crearProceso(nuevoProceso("Compras"), empresaA);
        Actividad actividad = actividadService.crearActividad(
                nuevaActividad("Revisar solicitud", 10, 10, 1L), proceso.getId(), empresaA);

        ActividadDTO cambios = nuevaActividad("Aprobar solicitud", 30, 40, 5L);
        cambios.setTipoActividad("TAREA_SERVICIO");

        Actividad editada = actividadService.actualizarActividad(actividad.getId(), cambios, empresaA);

        assertEquals("Aprobar solicitud", editada.getNombre());
        assertEquals("TAREA_SERVICIO", editada.getTipoActividad());
        assertEquals(5L, editada.getLaneId());
    }

    @Test
    void alEditarNoSePuedeChocarConElNombreDeOtraActividad() {
        Proceso proceso = procesoService.crearProceso(nuevoProceso("Compras"), empresaA);
        actividadService.crearActividad(
                nuevaActividad("Revisar solicitud", 10, 10, 1L), proceso.getId(), empresaA);
        Actividad segunda = actividadService.crearActividad(
                nuevaActividad("Aprobar solicitud", 20, 20, 1L), proceso.getId(), empresaA);

        assertThrows(IllegalArgumentException.class,
                () -> actividadService.actualizarActividad(
                        segunda.getId(), nuevaActividad("Revisar solicitud", 20, 20, 1L), empresaA));
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

    // ---------- HU-10 - Eliminar actividad ----------

    @Test
    void laActividadEliminadaDesapareceDelListado() {
        Proceso proceso = procesoService.crearProceso(nuevoProceso("Compras"), empresaA);
        Actividad actividad = actividadService.crearActividad(
                nuevaActividad("Revisar solicitud", 10, 10, 1L), proceso.getId(), empresaA);

        actividadService.eliminarActividad(actividad.getId(), empresaA);

        assertTrue(actividadService.listarPorProcesoYEmpresa(proceso.getId(), empresaA).isEmpty());
    }

    @Test
    void noSePuedeEliminarLaActividadDeOtraEmpresa() {
        Proceso proceso = procesoService.crearProceso(nuevoProceso("Compras"), empresaA);
        Actividad actividad = actividadService.crearActividad(
                nuevaActividad("Revisar solicitud", 10, 10, 1L), proceso.getId(), empresaA);

        assertThrows(IllegalArgumentException.class,
                () -> actividadService.eliminarActividad(actividad.getId(), empresaB));
    }

    // ---------- utilidades ----------

    private Long crearEmpresa(String nombre, String nit) {
        EmpresaDTO empresa = new EmpresaDTO();
        empresa.setNombre(nombre);
        empresa.setNit(nit);
        empresa.setCorreo("contacto@" + nit + ".com");
        return empresaService.registrarEmpresa(empresa).getId();
    }

    private ProcesoDTO nuevoProceso(String nombre) {
        ProcesoDTO proceso = new ProcesoDTO();
        proceso.setNombre(nombre);
        proceso.setDescripcion("Proceso de prueba");
        proceso.setCategoria("Operaciones");
        return proceso;
    }

    private ActividadDTO nuevaActividad(String nombre, Integer x, Integer y, Long laneId) {
        ActividadDTO actividad = new ActividadDTO();
        actividad.setNombre(nombre);
        actividad.setTipoActividad("TAREA_USUARIO");
        actividad.setPosicionX(x);
        actividad.setPosicionY(y);
        actividad.setLaneId(laneId);
        return actividad;
    }
}

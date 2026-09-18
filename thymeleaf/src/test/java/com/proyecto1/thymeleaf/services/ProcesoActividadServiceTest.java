package com.proyecto1.thymeleaf.services;

import com.proyecto1.thymeleaf.dto.ActividadRequestDTO;
import com.proyecto1.thymeleaf.dto.ActividadResponseDTO;
import com.proyecto1.thymeleaf.dto.ProcesoRequestDTO;
import com.proyecto1.thymeleaf.dto.ProcesoResponseDTO;
import com.proyecto1.thymeleaf.model.Empresa;
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
        ProcesoResponseDTO guardado = procesoService.crearProceso(nuevoProceso("Compras"), empresaA);

        assertEquals("Compras", guardado.getNombre());
        assertEquals("Proceso de prueba", guardado.getDescripcion());
        assertEquals("Operaciones", guardado.getCategoria());
    }

    @Test
    void elProcesoNaceEnBorrador() {
        ProcesoResponseDTO guardado = procesoService.crearProceso(nuevoProceso("Compras"), empresaA);

        assertEquals("BORRADOR", guardado.getEstado());
    }

    @Test
    void elProcesoPuedePasarAPublicado() {
        ProcesoResponseDTO guardado = procesoService.crearProceso(nuevoProceso("Compras"), empresaA);

        ProcesoResponseDTO publicado = procesoService.publicarProceso(guardado.getId(), empresaA);

        assertEquals("PUBLICADO", publicado.getEstado());
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

        ProcesoResponseDTO deLaOtraEmpresa = procesoService.crearProceso(nuevoProceso("Compras"), empresaB);

        assertEquals("Compras", deLaOtraEmpresa.getNombre());
    }

    @Test
    void unaEmpresaNoAlcanzaLosProcesosDeOtra() {
        ProcesoResponseDTO deA = procesoService.crearProceso(nuevoProceso("Compras"), empresaA);

        assertThrows(IllegalArgumentException.class,
                () -> procesoService.obtenerPorIdYEmpresa(deA.getId(), empresaB));
    }

    @Test
    void elNombreDelProcesoEsObligatorio() {
        ProcesoRequestDTO sinNombre = new ProcesoRequestDTO();
        sinNombre.setNombre("   ");
        sinNombre.setDescripcion("Desc");
        sinNombre.setCategoria("Cat");

        assertThrows(IllegalArgumentException.class,
                () -> procesoService.crearProceso(sinNombre, empresaA));
    }

    // ---------- HU-08 - Crear actividad ----------

    @Test
    void laActividadQuedaAsociadaAlProcesoALaLaneYALaPosicionIndicada() {
        ProcesoResponseDTO proceso = procesoService.crearProceso(nuevoProceso("Compras"), empresaA);

        ActividadResponseDTO guardada = actividadService.crearActividad(
                nuevaActividad("Revisar solicitud", 120, 45, 7L), proceso.getId(), empresaA);

        assertEquals("Revisar solicitud", guardada.getNombre());
        assertEquals("TAREA_USUARIO", guardada.getTipoActividad());
        assertEquals(7L, guardada.getLaneId());
        assertEquals(120, guardada.getPosicionX());
        assertEquals(45, guardada.getPosicionY());
    }

    @Test
    void elNombreDeLaActividadEsUnicoDentroDelProceso() {
        ProcesoResponseDTO proceso = procesoService.crearProceso(nuevoProceso("Compras"), empresaA);
        actividadService.crearActividad(
                nuevaActividad("Revisar solicitud", 10, 10, 1L), proceso.getId(), empresaA);

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> actividadService.crearActividad(
                        nuevaActividad("Revisar solicitud", 80, 80, 2L), proceso.getId(), empresaA));

        assertTrue(error.getMessage().contains("Ya existe una actividad"));
    }

    @Test
    void otroProcesoSiPuedeUsarElMismoNombreDeActividad() {
        ProcesoResponseDTO compras = procesoService.crearProceso(nuevoProceso("Compras"), empresaA);
        ProcesoResponseDTO ventas = procesoService.crearProceso(nuevoProceso("Ventas"), empresaA);
        actividadService.crearActividad(
                nuevaActividad("Revisar solicitud", 10, 10, 1L), compras.getId(), empresaA);

        ActividadResponseDTO enVentas = actividadService.crearActividad(
                nuevaActividad("Revisar solicitud", 10, 10, 1L), ventas.getId(), empresaA);

        assertEquals(ventas.getId(), enVentas.getId() != null ? ventas.getId() : null);
    }

    @Test
    void noSePuedenAgregarActividadesAlProcesoDeOtraEmpresa() {
        ProcesoResponseDTO deA = procesoService.crearProceso(nuevoProceso("Compras"), empresaA);

        assertThrows(IllegalArgumentException.class,
                () -> actividadService.crearActividad(
                        nuevaActividad("Revisar solicitud", 10, 10, 1L), deA.getId(), empresaB));
    }

    @Test
    void laActividadExigeNombreTipoLaneYPosicion() {
        ProcesoResponseDTO proceso = procesoService.crearProceso(nuevoProceso("Compras"), empresaA);
        Long procesoId = proceso.getId();

        ActividadRequestDTO sinNombre = nuevaActividad("  ", 10, 10, 1L);
        ActividadRequestDTO sinTipo = nuevaActividad("A", 10, 10, 1L);
        sinTipo.setTipoActividad(null);
        ActividadRequestDTO sinLane = nuevaActividad("B", 10, 10, null);
        ActividadRequestDTO sinPosicion = nuevaActividad("C", null, 10, 1L);

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
        ProcesoResponseDTO proceso = procesoService.crearProceso(nuevoProceso("Compras"), empresaA);
        ActividadResponseDTO actividad = actividadService.crearActividad(
                nuevaActividad("Revisar solicitud", 10, 10, 1L), proceso.getId(), empresaA);

        ActividadResponseDTO movida = actividadService.moverActividad(actividad.getId(), 300, 240, empresaA);

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

    private ProcesoRequestDTO nuevoProceso(String nombre) {
        ProcesoRequestDTO dto = new ProcesoRequestDTO();
        dto.setNombre(nombre);
        dto.setDescripcion("Proceso de prueba");
        dto.setCategoria("Operaciones");
        return dto;
    }

    private ActividadRequestDTO nuevaActividad(String nombre, Integer x, Integer y, Long laneId) {
        ActividadRequestDTO dto = new ActividadRequestDTO();
        dto.setNombre(nombre);
        dto.setTipoActividad("TAREA_USUARIO");
        dto.setPosicionX(x);
        dto.setPosicionY(y);
        dto.setLaneId(laneId);
        return dto;
    }
}

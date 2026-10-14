package com.proyecto1.thymeleaf.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.proyecto1.thymeleaf.dto.ProcesoUsoDTO;
import com.proyecto1.thymeleaf.dto.RolProcesoRequestDTO;
import com.proyecto1.thymeleaf.dto.RolProcesoRespuestaDTO;
import com.proyecto1.thymeleaf.model.Empresa;
import com.proyecto1.thymeleaf.model.RolProceso;
import com.proyecto1.thymeleaf.repository.RolProcesoRepository;

@ExtendWith(MockitoExtension.class)
class RolProcesoServiceTest {

    @Mock private RolProcesoRepository rolProcesoRepository;
    @Mock private EmpresaService empresaService;
    @Mock private UsoRolProceso usoRolProceso;

    @InjectMocks
    private RolProcesoService rolProcesoService;

    private RolProcesoRequestDTO datosValidos;
    private Empresa empresa;
    private RolProceso existente;

    @BeforeEach
    void setUp() {
        datosValidos = new RolProcesoRequestDTO();
        datosValidos.setNombre("Analista");
        datosValidos.setDescripcion("Revisa y valida las solicitudes");

        empresa = new Empresa();
        empresa.setId(1L);

        existente = new RolProceso();
        existente.setId(5L);
        existente.setNombre("Supervisor");
        existente.setDescripcion("Aprueba");
        existente.setEmpresa(empresa);
    }

    // ---------- HU-17 crear ----------

    @Test
    void crearRol_conDatosValidos_guardaEnLaEmpresaYDevuelveElRol() {
        datosValidos.setNombre("  Analista  ");
        when(empresaService.obtenerPorId(1L)).thenReturn(empresa);
        when(rolProcesoRepository.existsByNombreAndEmpresaId("Analista", 1L)).thenReturn(false);
        when(rolProcesoRepository.save(any(RolProceso.class))).thenAnswer(inv -> inv.getArgument(0));

        RolProcesoRespuestaDTO resultado = rolProcesoService.crearRol(datosValidos, 1L);

        assertEquals("Analista", resultado.nombre());
        assertEquals("Revisa y valida las solicitudes", resultado.descripcion());
        assertTrue(resultado.eliminable());
        assertTrue(resultado.procesosEnUso().isEmpty());
    }

    @Test
    void crearRol_conNombreDuplicadoEnLaEmpresa_lanzaExcepcionYNoGuarda() {
        when(empresaService.obtenerPorId(1L)).thenReturn(empresa);
        when(rolProcesoRepository.existsByNombreAndEmpresaId("Analista", 1L)).thenReturn(true);

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> rolProcesoService.crearRol(datosValidos, 1L));

        assertEquals("Ya existe un rol llamado 'Analista' en la empresa", e.getMessage());
        verify(rolProcesoRepository, never()).save(any(RolProceso.class));
    }

    @Test
    void crearRol_conNombreVacio_lanzaExcepcion() {
        datosValidos.setNombre("   ");
        when(empresaService.obtenerPorId(1L)).thenReturn(empresa);

        assertThrows(IllegalArgumentException.class, () -> rolProcesoService.crearRol(datosValidos, 1L));
        verify(rolProcesoRepository, never()).save(any(RolProceso.class));
    }

    @Test
    void crearRol_conDescripcionVacia_lanzaExcepcion() {
        datosValidos.setDescripcion(null);
        when(empresaService.obtenerPorId(1L)).thenReturn(empresa);

        assertThrows(IllegalArgumentException.class, () -> rolProcesoService.crearRol(datosValidos, 1L));
        verify(rolProcesoRepository, never()).save(any(RolProceso.class));
    }

    @Test
    void crearRol_conEmpresaInexistente_lanzaExcepcion() {
        when(empresaService.obtenerPorId(1L)).thenThrow(new IllegalArgumentException("La empresa no existe"));

        assertThrows(IllegalArgumentException.class, () -> rolProcesoService.crearRol(datosValidos, 1L));
        verify(rolProcesoRepository, never()).save(any(RolProceso.class));
    }

    // ---------- HU-18 editar ----------

    @Test
    void actualizarRol_conDatosValidos_cambiaNombreYDescripcion() {
        when(rolProcesoRepository.findByIdAndEmpresaId(5L, 1L)).thenReturn(Optional.of(existente));
        when(rolProcesoRepository.existsByNombreAndEmpresaIdAndIdNot("Analista", 1L, 5L)).thenReturn(false);
        when(rolProcesoRepository.save(any(RolProceso.class))).thenAnswer(inv -> inv.getArgument(0));

        RolProcesoRespuestaDTO resultado = rolProcesoService.actualizarRol(5L, datosValidos, 1L);

        assertEquals(5L, resultado.id());
        assertEquals("Analista", resultado.nombre());
        assertEquals("Revisa y valida las solicitudes", resultado.descripcion());
    }

    @Test
    void actualizarRol_conNombreDeOtroRolDeLaEmpresa_lanzaExcepcion() {
        when(rolProcesoRepository.findByIdAndEmpresaId(5L, 1L)).thenReturn(Optional.of(existente));
        when(rolProcesoRepository.existsByNombreAndEmpresaIdAndIdNot("Analista", 1L, 5L)).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> rolProcesoService.actualizarRol(5L, datosValidos, 1L));
        verify(rolProcesoRepository, never()).save(any(RolProceso.class));
    }

    @Test
    void actualizarRol_inexistenteOAjeno_lanzaExcepcion() {
        when(rolProcesoRepository.findByIdAndEmpresaId(99L, 1L)).thenReturn(Optional.empty());

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> rolProcesoService.actualizarRol(99L, datosValidos, 1L));

        assertEquals("El rol no existe o no pertenece a su empresa", e.getMessage());
    }

    @Test
    void actualizarRol_conNombreVacio_lanzaExcepcion() {
        datosValidos.setNombre("");
        when(rolProcesoRepository.findByIdAndEmpresaId(5L, 1L)).thenReturn(Optional.of(existente));

        assertThrows(IllegalArgumentException.class, () -> rolProcesoService.actualizarRol(5L, datosValidos, 1L));
        verify(rolProcesoRepository, never()).save(any(RolProceso.class));
    }

    // ---------- HU-19 eliminar ----------

    @Test
    void eliminarRol_sinUso_hacePorBorradoLogico() {
        when(rolProcesoRepository.findByIdAndEmpresaId(5L, 1L)).thenReturn(Optional.of(existente));
        when(usoRolProceso.procesosDondeSeUsa(5L, 1L)).thenReturn(List.of());

        rolProcesoService.eliminarRol(5L, 1L);

        verify(rolProcesoRepository).delete(existente);
    }

    @Test
    void eliminarRol_enUso_noElimina_ydiceEnQueProcesosEsta() {
        when(rolProcesoRepository.findByIdAndEmpresaId(5L, 1L)).thenReturn(Optional.of(existente));
        when(usoRolProceso.procesosDondeSeUsa(5L, 1L)).thenReturn(List.of(
                new ProcesoUsoDTO(10L, "Solicitud de vacaciones"),
                new ProcesoUsoDTO(11L, "Compra de insumos")));

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> rolProcesoService.eliminarRol(5L, 1L));

        assertTrue(e.getMessage().contains("Supervisor"));
        assertTrue(e.getMessage().contains("Solicitud de vacaciones"));
        assertTrue(e.getMessage().contains("Compra de insumos"));
        verify(rolProcesoRepository, never()).delete(any(RolProceso.class));
    }

    @Test
    void eliminarRol_inexistenteOAjeno_lanzaExcepcion() {
        when(rolProcesoRepository.findByIdAndEmpresaId(99L, 1L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> rolProcesoService.eliminarRol(99L, 1L));
        verify(rolProcesoRepository, never()).delete(any(RolProceso.class));
    }

    // ---------- HU-20 consultar ----------

    @Test
    void listarPorEmpresa_devuelveLosRolesConSuUso() {
        when(rolProcesoRepository.findByEmpresaId(1L)).thenReturn(List.of(existente));
        when(usoRolProceso.procesosDondeSeUsa(5L, 1L)).thenReturn(List.of(new ProcesoUsoDTO(10L, "Vacaciones")));

        List<RolProcesoRespuestaDTO> resultado = rolProcesoService.listarPorEmpresa(1L);

        assertEquals(1, resultado.size());
        assertEquals("Supervisor", resultado.get(0).nombre());
        assertFalse(resultado.get(0).eliminable());
        assertEquals("Vacaciones", resultado.get(0).procesosEnUso().get(0).nombre());
    }

    @Test
    void buscar_conTextoEnBlanco_noAplicaFiltroDeNombre() {
        Pageable pagina = PageRequest.of(0, 10);
        when(rolProcesoRepository.buscar(1L, null, pagina)).thenReturn(new PageImpl<>(List.of(existente)));

        Page<RolProcesoRespuestaDTO> resultado = rolProcesoService.buscar(1L, "   ", pagina);

        assertEquals(1, resultado.getTotalElements());
        assertEquals("Supervisor", resultado.getContent().get(0).nombre());
        assertTrue(resultado.getContent().get(0).eliminable());
    }

    @Test
    void buscar_conTexto_loRecortaYLoPasaAlRepositorio() {
        Pageable pagina = PageRequest.of(0, 10);
        when(rolProcesoRepository.buscar(1L, "sup", pagina)).thenReturn(new PageImpl<>(List.of(existente)));

        Page<RolProcesoRespuestaDTO> resultado = rolProcesoService.buscar(1L, " sup ", pagina);

        assertEquals(1, resultado.getContent().size());
    }

    @Test
    void obtenerPorIdYEmpresa_devuelveElRol() {
        when(rolProcesoRepository.findByIdAndEmpresaId(5L, 1L)).thenReturn(Optional.of(existente));

        RolProcesoRespuestaDTO resultado = rolProcesoService.obtenerPorIdYEmpresa(5L, 1L);

        assertEquals(5L, resultado.id());
        assertEquals("Supervisor", resultado.nombre());
    }

    @Test
    void obtenerPorIdYEmpresa_deOtraEmpresa_lanzaExcepcion() {
        when(rolProcesoRepository.findByIdAndEmpresaId(5L, 2L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> rolProcesoService.obtenerPorIdYEmpresa(5L, 2L));
    }
}

package com.proyecto1.thymeleaf.services;

import com.proyecto1.thymeleaf.dto.ProcesoDTO;
import com.proyecto1.thymeleaf.model.Empresa;
import com.proyecto1.thymeleaf.model.Proceso;
import com.proyecto1.thymeleaf.repository.EmpresaRepository;
import com.proyecto1.thymeleaf.repository.ProcesoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProcesoServiceTest {

    @Mock private ProcesoRepository procesoRepository;
    @Mock private EmpresaRepository empresaRepository;

    @InjectMocks
    private ProcesoService procesoService;

    private ProcesoDTO datosValidos;
    private Empresa empresa;

    @BeforeEach
    void setUp() {
        datosValidos = new ProcesoDTO();
        datosValidos.setNombre("Solicitud de vacaciones");
        datosValidos.setDescripcion("Proceso de RRHH");
        datosValidos.setCategoria("RRHH");

        empresa = new Empresa();
        empresa.setId(1L);
    }

    @Test
    void crearProceso_conDatosValidos_naceEnBorrador() {
        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));
        when(procesoRepository.existsByNombreAndEmpresaId("Solicitud de vacaciones", 1L)).thenReturn(false);
        when(procesoRepository.save(any(Proceso.class))).thenAnswer(inv -> inv.getArgument(0));

        Proceso resultado = procesoService.crearProceso(datosValidos, 1L);

        assertEquals(Proceso.EstadoProceso.BORRADOR, resultado.getEstado());
        assertEquals(empresa, resultado.getEmpresa());
    }

    @Test
    void crearProceso_conEmpresaInexistente_lanzaExcepcion() {
        when(empresaRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> procesoService.crearProceso(datosValidos, 1L));
    }

    @Test
    void crearProceso_conNombreDuplicadoEnLaEmpresa_lanzaExcepcion() {
        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));
        when(procesoRepository.existsByNombreAndEmpresaId("Solicitud de vacaciones", 1L)).thenReturn(true);
        assertThrows(IllegalArgumentException.class, () -> procesoService.crearProceso(datosValidos, 1L));
    }

    @Test
    void crearProceso_conDescripcionVacia_lanzaExcepcion() {
        datosValidos.setDescripcion(" ");
        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));
        assertThrows(IllegalArgumentException.class, () -> procesoService.crearProceso(datosValidos, 1L));
    }

    @Test
    void crearProceso_conCategoriaVacia_lanzaExcepcion() {
        datosValidos.setCategoria(" ");
        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));
        assertThrows(IllegalArgumentException.class, () -> procesoService.crearProceso(datosValidos, 1L));
    }

    @Test
    void obtenerPorIdYEmpresa_existente_devuelveElProceso() {
        Proceso proceso = new Proceso();
        proceso.setId(1L);
        proceso.setEmpresa(empresa);
        when(procesoRepository.findByIdAndEmpresaId(1L, 1L)).thenReturn(Optional.of(proceso));

        assertEquals(1L, procesoService.obtenerPorIdYEmpresa(1L, 1L).getId());
    }

    @Test
    void obtenerPorIdYEmpresa_inexistente_lanzaExcepcion() {
        when(procesoRepository.findByIdAndEmpresaId(1L, 1L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> procesoService.obtenerPorIdYEmpresa(1L, 1L));
    }

    @Test
    void actualizarProceso_conDatosValidos_actualizaYGuarda() {
        Proceso existente = new Proceso();
        existente.setId(1L);
        existente.setEmpresa(empresa);

        when(procesoRepository.findByIdAndEmpresaId(1L, 1L)).thenReturn(Optional.of(existente));
        when(procesoRepository.existsByNombreAndEmpresaIdAndIdNot("Solicitud de vacaciones", 1L, 1L)).thenReturn(false);
        when(procesoRepository.save(any(Proceso.class))).thenAnswer(inv -> inv.getArgument(0));

        Proceso actualizado = procesoService.actualizarProceso(1L, datosValidos, 1L);

        assertEquals("Solicitud de vacaciones", actualizado.getNombre());
    }

    @Test
    void actualizarProceso_conNombreYaUsadoPorOtro_lanzaExcepcion() {
        Proceso existente = new Proceso();
        existente.setId(1L);
        existente.setEmpresa(empresa);

        when(procesoRepository.findByIdAndEmpresaId(1L, 1L)).thenReturn(Optional.of(existente));
        when(procesoRepository.existsByNombreAndEmpresaIdAndIdNot("Solicitud de vacaciones", 1L, 1L)).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> procesoService.actualizarProceso(1L, datosValidos, 1L));
    }

    @Test
    void publicarProceso_desdeBorrador_pasaAPublicado() {
        Proceso proceso = new Proceso();
        proceso.setId(1L);
        proceso.setEmpresa(empresa);
        proceso.setEstado(Proceso.EstadoProceso.BORRADOR);

        when(procesoRepository.findByIdAndEmpresaId(1L, 1L)).thenReturn(Optional.of(proceso));
        when(procesoRepository.save(any(Proceso.class))).thenAnswer(inv -> inv.getArgument(0));

        Proceso resultado = procesoService.publicarProceso(1L, 1L);

        assertEquals(Proceso.EstadoProceso.PUBLICADO, resultado.getEstado());
    }

    @Test
    void publicarProceso_yaPublicado_lanzaExcepcion() {
        Proceso proceso = new Proceso();
        proceso.setId(1L);
        proceso.setEmpresa(empresa);
        proceso.setEstado(Proceso.EstadoProceso.PUBLICADO);

        when(procesoRepository.findByIdAndEmpresaId(1L, 1L)).thenReturn(Optional.of(proceso));

        assertThrows(IllegalArgumentException.class, () -> procesoService.publicarProceso(1L, 1L));
    }

    @Test
    void inactivarProceso_cambiaEstadoAInactivo() {
        Proceso proceso = new Proceso();
        proceso.setId(1L);
        proceso.setEmpresa(empresa);
        proceso.setEstado(Proceso.EstadoProceso.PUBLICADO);

        when(procesoRepository.findByIdAndEmpresaId(1L, 1L)).thenReturn(Optional.of(proceso));
        when(procesoRepository.save(any(Proceso.class))).thenAnswer(inv -> inv.getArgument(0));

        assertEquals(Proceso.EstadoProceso.INACTIVO, procesoService.inactivarProceso(1L, 1L).getEstado());
    }

    @Test
    void reactivarProceso_desdeInactivo_vuelveABorrador() {
        Proceso proceso = new Proceso();
        proceso.setId(1L);
        proceso.setEmpresa(empresa);
        proceso.setEstado(Proceso.EstadoProceso.INACTIVO);

        when(procesoRepository.findByIdAndEmpresaId(1L, 1L)).thenReturn(Optional.of(proceso));
        when(procesoRepository.save(any(Proceso.class))).thenAnswer(inv -> inv.getArgument(0));

        assertEquals(Proceso.EstadoProceso.BORRADOR, procesoService.reactivarProceso(1L, 1L).getEstado());
    }

    @Test
    void reactivarProceso_queNoEstaInactivo_lanzaExcepcion() {
        Proceso proceso = new Proceso();
        proceso.setId(1L);
        proceso.setEmpresa(empresa);
        proceso.setEstado(Proceso.EstadoProceso.BORRADOR);

        when(procesoRepository.findByIdAndEmpresaId(1L, 1L)).thenReturn(Optional.of(proceso));

        assertThrows(IllegalArgumentException.class, () -> procesoService.reactivarProceso(1L, 1L));
    }

    @Test
    void eliminarProceso_existente_invocaDelete() {
        Proceso proceso = new Proceso();
        proceso.setId(1L);
        proceso.setEmpresa(empresa);

        when(procesoRepository.findByIdAndEmpresaId(1L, 1L)).thenReturn(Optional.of(proceso));

        procesoService.eliminarProceso(1L, 1L);

        verify(procesoRepository).delete(proceso);
    }

    @Test
    void listarPorEmpresa_sinIncluirInactivos_delegaAlMetodoFiltrado() {
        when(procesoRepository.findByEmpresaIdAndEstadoNot(1L, Proceso.EstadoProceso.INACTIVO))
                .thenReturn(java.util.List.of(new Proceso()));

        assertEquals(1, procesoService.listarPorEmpresa(1L, false).size());
    }

    @Test
    void listarPorEmpresa_sobrecargaDeUnSoloArgumento_usaFalsoPorDefecto() {
        when(procesoRepository.findByEmpresaIdAndEstadoNot(1L, Proceso.EstadoProceso.INACTIVO))
                .thenReturn(java.util.List.of(new Proceso(), new Proceso()));

        assertEquals(2, procesoService.listarPorEmpresa(1L).size());
    }

    @Test
    void listarPorEmpresa_incluyendoInactivos_delegaAlMetodoCompleto() {
        when(procesoRepository.findByEmpresaId(1L)).thenReturn(java.util.List.of(new Proceso(), new Proceso()));

        assertEquals(2, procesoService.listarPorEmpresa(1L, true).size());
    }

    @Test
    void buscar_delegaAlRepositorioConElEstadoInactivoFijo() {
        Proceso proceso = new Proceso();
        proceso.setId(1L);
        proceso.setNombre("Compra de insumos");

        Page<Proceso> pagina = new PageImpl<>(java.util.List.of(proceso));
        Pageable pageable = mock(Pageable.class);

        when(procesoRepository.buscar(eq(1L), eq("compra"), isNull(), eq(false),
                eq(Proceso.EstadoProceso.INACTIVO), isNull(), eq(pageable))).thenReturn(pagina);

        Page<Proceso> resultado = procesoService.buscar(1L, "compra", null, null, false, pageable);

        assertEquals(1, resultado.getTotalElements());
    }
}
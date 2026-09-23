package com.proyecto1.thymeleaf.repository;

import com.proyecto1.thymeleaf.model.Empresa;
import com.proyecto1.thymeleaf.model.Proceso;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;


@DataJpaTest
@ActiveProfiles("h2")
class ProcesoRepositoryTest {

    @Autowired private ProcesoRepository procesoRepository;
    @Autowired private EmpresaRepository empresaRepository;

    private Empresa empresaA;
    private Empresa empresaB;

    @BeforeEach
    void setUp() {
        empresaA = new Empresa();
        empresaA.setNombre("Empresa A");
        empresaA.setNit("900111111-1");
        empresaA.setCorreo("a@demo.com");
        empresaA = empresaRepository.save(empresaA);

        empresaB = new Empresa();
        empresaB.setNombre("Empresa B");
        empresaB.setNit("900222222-2");
        empresaB.setCorreo("b@demo.com");
        empresaB = empresaRepository.save(empresaB);

        guardarProceso("Compra de insumos", "RRHH", Proceso.EstadoProceso.BORRADOR, empresaA);
        guardarProceso("Solicitud de vacaciones", "RRHH", Proceso.EstadoProceso.PUBLICADO, empresaA);
        guardarProceso("Proceso inactivo", "Comercial", Proceso.EstadoProceso.INACTIVO, empresaA);
        guardarProceso("Proceso de otra empresa", "RRHH", Proceso.EstadoProceso.BORRADOR, empresaB);
    }

    private void guardarProceso(String nombre, String categoria, Proceso.EstadoProceso estado, Empresa empresa) {
        Proceso proceso = new Proceso();
        proceso.setNombre(nombre);
        proceso.setDescripcion("Descripcion de prueba");
        proceso.setCategoria(categoria);
        proceso.setEstado(estado);
        proceso.setEmpresa(empresa);
        procesoRepository.save(proceso);
    }

    @Test
    void findByEmpresaId_devuelveSoloLosDeEsaEmpresa() {
        assertEquals(3, procesoRepository.findByEmpresaId(empresaA.getId()).size());
        assertEquals(1, procesoRepository.findByEmpresaId(empresaB.getId()).size());
    }

    @Test
    void findByEmpresaIdAndEstadoNot_excluyeLosInactivos() {
        var activos = procesoRepository.findByEmpresaIdAndEstadoNot(empresaA.getId(), Proceso.EstadoProceso.INACTIVO);
        assertEquals(2, activos.size());
        assertTrue(activos.stream().noneMatch(p -> p.getEstado() == Proceso.EstadoProceso.INACTIVO));
    }

    @Test
    void findByIdAndEmpresaId_conEmpresaCorrecta_loEncuentra() {
        Proceso alguno = procesoRepository.findByEmpresaId(empresaA.getId()).get(0);

        Optional<Proceso> encontrado = procesoRepository.findByIdAndEmpresaId(alguno.getId(), empresaA.getId());

        assertTrue(encontrado.isPresent());
    }

    @Test
    void findByIdAndEmpresaId_conEmpresaIncorrecta_noLoEncuentra() {
        Proceso deLaEmpresaA = procesoRepository.findByEmpresaId(empresaA.getId()).get(0);

        Optional<Proceso> resultado = procesoRepository.findByIdAndEmpresaId(deLaEmpresaA.getId(), empresaB.getId());

        assertTrue(resultado.isEmpty());
    }

    @Test
    void existsByNombreAndEmpresaId_esInsensibleAMayusculas() {
        assertTrue(procesoRepository.existsByNombreAndEmpresaId("compra de insumos", empresaA.getId()));
        assertFalse(procesoRepository.existsByNombreAndEmpresaId("compra de insumos", empresaB.getId()));
    }

    @Test
    void existsByNombreAndEmpresaIdAndIdNot_excluyeElPropioId() {
        Proceso proceso = procesoRepository.findByEmpresaId(empresaA.getId()).stream()
                .filter(p -> "Compra de insumos".equals(p.getNombre()))
                .findFirst().orElseThrow();

        // Contra si mismo, excluyendo su propio id: no deberia contar como duplicado
        assertFalse(procesoRepository.existsByNombreAndEmpresaIdAndIdNot(
                "Compra de insumos", empresaA.getId(), proceso.getId()));
    }

    @Test
    void buscar_filtraPorNombreYExcluyeInactivosPorDefecto() {
        Pageable pageable = PageRequest.of(0, 10);

        var resultado = procesoRepository.buscar(empresaA.getId(), "compra", null, false,
                Proceso.EstadoProceso.INACTIVO, null, pageable);

        assertEquals(1, resultado.getTotalElements());
        assertEquals("Compra de insumos", resultado.getContent().get(0).getNombre());
    }

    @Test
    void buscar_conIncluirInactivosTrue_losMuestra() {
        Pageable pageable = PageRequest.of(0, 10);

        var resultado = procesoRepository.buscar(empresaA.getId(), null, null, true,
                Proceso.EstadoProceso.INACTIVO, null, pageable);

        assertEquals(3, resultado.getTotalElements());
    }

    @Test
    void buscar_filtraPorCategoria() {
        Pageable pageable = PageRequest.of(0, 10);

        var resultado = procesoRepository.buscar(empresaA.getId(), null, null, false,
                Proceso.EstadoProceso.INACTIVO, "Comercial", pageable);

        assertEquals(0, resultado.getTotalElements());
    }

    @Test
    void buscar_nuncaCruzaEmpresas() {
        Pageable pageable = PageRequest.of(0, 10);

        var resultado = procesoRepository.buscar(empresaA.getId(), null, null, true,
                Proceso.EstadoProceso.INACTIVO, null, pageable);

        assertTrue(resultado.getContent().stream()
                .allMatch(p -> p.getEmpresa().getId().equals(empresaA.getId())));
    }
}
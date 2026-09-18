package com.proyecto1.thymeleaf.services;

import com.proyecto1.thymeleaf.dto.EmpresaDTO;
import com.proyecto1.thymeleaf.dto.ProcesoDTO;
import com.proyecto1.thymeleaf.model.Proceso;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * HU-07 Consultar procesos: activos por defecto, filtro de inactivos,
 * busqueda por nombre, filtros por estado y categoria, paginacion, y
 * aislamiento por empresa en todos los casos.
 */
@SpringBootTest
@ActiveProfiles("h2")
@Transactional
class ProcesoBusquedaServiceTest {

    @Autowired
    private EmpresaService empresaService;

    @Autowired
    private ProcesoService procesoService;

    private Long empresaA;
    private Long empresaB;

    @BeforeEach
    void preparar() {
        empresaA = crearEmpresa("Empresa A Busq", "900710001");
        empresaB = crearEmpresa("Empresa B Busq", "900710002");

        crear("Compras nacionales", "Operaciones", empresaA);
        crear("Compras internacionales", "Operaciones", empresaA);
        crear("Contratacion", "Talento humano", empresaA);
        crear("Compras de la otra empresa", "Operaciones", empresaB);
    }

    @Test
    void elListadoPorDefectoExcluyeLosInactivos() {
        Proceso contratacion = buscarPorNombre("Contratacion");
        procesoService.inactivarProceso(contratacion.getId(), empresaA);

        List<Proceso> activos = procesoService.listarPorEmpresa(empresaA);
        List<Proceso> todos = procesoService.listarPorEmpresa(empresaA, true);

        assertEquals(2, activos.size());
        assertEquals(3, todos.size());
        assertTrue(activos.stream().noneMatch(p -> p.getEstado() == Proceso.EstadoProceso.INACTIVO));
    }

    @Test
    void reactivarDevuelveElProcesoABorrador() {
        Proceso p = buscarPorNombre("Contratacion");
        procesoService.inactivarProceso(p.getId(), empresaA);

        Proceso reactivado = procesoService.reactivarProceso(p.getId(), empresaA);

        assertEquals(Proceso.EstadoProceso.BORRADOR, reactivado.getEstado());
        assertThrows(IllegalArgumentException.class,
                () -> procesoService.reactivarProceso(p.getId(), empresaA), "No estaba inactivo");
    }

    @Test
    void buscaPorNombreSinDistinguirMayusculas() {
        Page<Proceso> resultado = procesoService.buscar(empresaA, "COMPRAS", null, null, false, pagina(0, 10));

        assertEquals(2, resultado.getTotalElements());
        assertTrue(resultado.getContent().stream().allMatch(p -> p.getNombre().startsWith("Compras")));
    }

    @Test
    void filtraPorEstado() {
        Proceso nacionales = buscarPorNombre("Compras nacionales");
        procesoService.publicarProceso(nacionales.getId(), empresaA);

        Page<Proceso> publicados = procesoService.buscar(empresaA, null, Proceso.EstadoProceso.PUBLICADO, null, false, pagina(0, 10));
        Page<Proceso> borradores = procesoService.buscar(empresaA, null, Proceso.EstadoProceso.BORRADOR, null, false, pagina(0, 10));

        assertEquals(1, publicados.getTotalElements());
        assertEquals("Compras nacionales", publicados.getContent().get(0).getNombre());
        assertEquals(2, borradores.getTotalElements());
    }

    @Test
    void filtraPorCategoria() {
        Page<Proceso> talento = procesoService.buscar(empresaA, null, null, "talento humano", false, pagina(0, 10));

        assertEquals(1, talento.getTotalElements());
        assertEquals("Contratacion", talento.getContent().get(0).getNombre());
    }

    @Test
    void combinaFiltrosYExcluyeInactivosSalvoQueSePidan() {
        Proceso internacionales = buscarPorNombre("Compras internacionales");
        procesoService.inactivarProceso(internacionales.getId(), empresaA);

        Page<Proceso> sinInactivos = procesoService.buscar(empresaA, "compras", null, "Operaciones", false, pagina(0, 10));
        Page<Proceso> conInactivos = procesoService.buscar(empresaA, "compras", null, "Operaciones", true, pagina(0, 10));
        Page<Proceso> soloInactivos = procesoService.buscar(empresaA, null, Proceso.EstadoProceso.INACTIVO, null, false, pagina(0, 10));

        assertEquals(1, sinInactivos.getTotalElements());
        assertEquals(2, conInactivos.getTotalElements());
        assertEquals(1, soloInactivos.getTotalElements());
    }

    @Test
    void paginaLosResultados() {
        Page<Proceso> primera = procesoService.buscar(empresaA, null, null, null, false, pagina(0, 2));
        Page<Proceso> segunda = procesoService.buscar(empresaA, null, null, null, false, pagina(1, 2));

        assertEquals(3, primera.getTotalElements());
        assertEquals(2, primera.getTotalPages());
        assertEquals(2, primera.getContent().size());
        assertEquals(1, segunda.getContent().size());
        assertFalse(primera.getContent().get(0).getNombre().equals(segunda.getContent().get(0).getNombre()));
    }

    @Test
    void laBusquedaNuncaCruzaDeEmpresa() {
        Page<Proceso> deB = procesoService.buscar(empresaB, "compras", null, null, true, pagina(0, 10));
        Page<Proceso> deA = procesoService.buscar(empresaA, "otra empresa", null, null, true, pagina(0, 10));

        assertEquals(1, deB.getTotalElements());
        assertEquals("Compras de la otra empresa", deB.getContent().get(0).getNombre());
        assertEquals(0, deA.getTotalElements());
    }

    // ---------- utilidades ----------

    private Long crearEmpresa(String nombre, String nit) {
        EmpresaDTO e = new EmpresaDTO();
        e.setNombre(nombre);
        e.setNit(nit);
        e.setCorreo("contacto" + nit + "@prueba.com");
        return empresaService.registrarEmpresa(e).getId();
    }

    private Proceso crear(String nombre, String categoria, Long empresaId) {
        ProcesoDTO p = new ProcesoDTO();
        p.setNombre(nombre);
        p.setDescripcion("Descripcion de " + nombre);
        p.setCategoria(categoria);
        return procesoService.crearProceso(p, empresaId);
    }

    private Proceso buscarPorNombre(String nombre) {
        return procesoService.listarPorEmpresa(empresaA, true).stream()
                .filter(p -> p.getNombre().equals(nombre))
                .findFirst().orElseThrow();
    }

    private PageRequest pagina(int numero, int tamano) {
        return PageRequest.of(numero, tamano, Sort.by("nombre").ascending());
    }
}

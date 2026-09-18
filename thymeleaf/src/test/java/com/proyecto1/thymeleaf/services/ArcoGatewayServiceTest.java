package com.proyecto1.thymeleaf.services;

import com.proyecto1.thymeleaf.dto.ArcoDTO;
import com.proyecto1.thymeleaf.dto.EmpresaDTO;
import com.proyecto1.thymeleaf.dto.GatewayDTO;
import com.proyecto1.thymeleaf.dto.ProcesoDTO;
import com.proyecto1.thymeleaf.model.Gateway;
import com.proyecto1.thymeleaf.model.Usuario;
import com.proyecto1.thymeleaf.security.UsuarioPrincipal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * HU-14 a HU-16 (gateways) y validaciones de HU-11 (arcos).
 *
 * Sobre los arcos: ElementoConectable es abstracta y ninguna entidad la
 * extiende (Actividad y Gateway son clases sueltas), asi que hoy no se puede
 * crear ningun arco real. Lo que si se puede, y aqui se prueba, es que cada
 * validacion rechace lo que debe antes de llegar a la base de datos.
 */
@SpringBootTest
@ActiveProfiles("h2")
@Transactional
class ArcoGatewayServiceTest {

    @Autowired
    private EmpresaService empresaService;

    @Autowired
    private ProcesoService procesoService;

    @Autowired
    private GatewayService gatewayService;

    @Autowired
    private ArcoService arcoService;

    private Long empresaA;
    private Long empresaB;
    private Long procesoA;

    @BeforeEach
    void preparar() {
        empresaA = crearEmpresa("Empresa A Gw", "900610001");
        empresaB = crearEmpresa("Empresa B Gw", "900610002");
        autenticarComoAdmin(empresaA);

        ProcesoDTO p = new ProcesoDTO();
        p.setNombre("Proceso con gateways");
        p.setDescripcion("Para probar compuertas y arcos");
        p.setCategoria("Operaciones");
        procesoA = procesoService.crearProceso(p, empresaA).getId();
    }

    @AfterEach
    void limpiar() {
        SecurityContextHolder.clearContext();
    }

    // ---------- HU-14 - Crear gateway ----------

    @Test
    void elGatewayQuedaAsociadoAlProcesoConSuTipo() {
        Gateway g = gatewayService.crearGateway(nuevoGateway("Aprobado?", Gateway.TipoGateway.EXCLUSIVO), procesoA, empresaA);

        assertEquals("Aprobado?", g.getNombre());
        assertEquals(Gateway.TipoGateway.EXCLUSIVO, g.getTipo());
        assertEquals(procesoA, g.getProceso().getId());
    }

    @Test
    void elTipoDeGatewayEsObligatorio() {
        GatewayDTO sinTipo = nuevoGateway("Sin tipo", null);

        assertThrows(IllegalArgumentException.class,
                () -> gatewayService.crearGateway(sinTipo, procesoA, empresaA));
    }

    @Test
    void losTresTiposDeGatewaySonValidos() {
        for (Gateway.TipoGateway tipo : Gateway.TipoGateway.values()) {
            Gateway g = gatewayService.crearGateway(nuevoGateway("Gw " + tipo, tipo), procesoA, empresaA);
            assertEquals(tipo, g.getTipo());
        }
        assertEquals(3, gatewayService.listarPorProcesoYEmpresa(procesoA, empresaA).size());
    }

    @Test
    void otraEmpresaNoPuedeCrearNiVerGatewaysEnElProceso() {
        gatewayService.crearGateway(nuevoGateway("Privado", Gateway.TipoGateway.PARALELO), procesoA, empresaA);

        assertThrows(IllegalArgumentException.class,
                () -> gatewayService.crearGateway(nuevoGateway("Intruso", Gateway.TipoGateway.PARALELO), procesoA, empresaB));
        assertThrows(IllegalArgumentException.class,
                () -> gatewayService.listarPorProcesoYEmpresa(procesoA, empresaB));
    }

    // ---------- HU-15 / HU-16 - Editar y eliminar gateway ----------

    @Test
    void elGatewaySePuedeEditarYEliminarLogicamente() {
        Gateway g = gatewayService.crearGateway(nuevoGateway("Original", Gateway.TipoGateway.EXCLUSIVO), procesoA, empresaA);

        Gateway editado = gatewayService.actualizarGateway(g.getId(), nuevoGateway("Editado", Gateway.TipoGateway.INCLUSIVO), empresaA);
        assertEquals("Editado", editado.getNombre());
        assertEquals(Gateway.TipoGateway.INCLUSIVO, editado.getTipo());

        gatewayService.eliminarGateway(g.getId(), empresaA);
        assertTrue(gatewayService.listarPorProcesoYEmpresa(procesoA, empresaA).isEmpty());
    }

    // ---------- HU-11 - Validaciones de arco ----------

    @Test
    void unArcoNoPuedeConectarUnElementoConsigoMismo() {
        ArcoDTO arco = new ArcoDTO();
        arco.setOrigenId(7L);
        arco.setDestinoId(7L);

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> arcoService.crearArco(arco, procesoA, empresaA));
        assertTrue(e.getMessage().contains("consigo mismo"));
    }

    @Test
    void unArcoExigeOrigenYDestino() {
        ArcoDTO sinDestino = new ArcoDTO();
        sinDestino.setOrigenId(1L);

        assertThrows(IllegalArgumentException.class,
                () -> arcoService.crearArco(sinDestino, procesoA, empresaA));
    }

    @Test
    void unArcoConReferenciasInexistentesEsRechazado() {
        ArcoDTO arco = new ArcoDTO();
        arco.setOrigenId(99991L);
        arco.setDestinoId(99992L);

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> arcoService.crearArco(arco, procesoA, empresaA));
        assertTrue(e.getMessage().contains("no existe"));
    }

    @Test
    void noSePuedenCrearArcosEnElProcesoDeOtraEmpresa() {
        ArcoDTO arco = new ArcoDTO();
        arco.setOrigenId(1L);
        arco.setDestinoId(2L);

        assertThrows(IllegalArgumentException.class,
                () -> arcoService.crearArco(arco, procesoA, empresaB));
    }

    // ---------- utilidades ----------

    private Long crearEmpresa(String nombre, String nit) {
        EmpresaDTO e = new EmpresaDTO();
        e.setNombre(nombre);
        e.setNit(nit);
        e.setCorreo("contacto" + nit + "@prueba.com");
        return empresaService.registrarEmpresa(e).getId();
    }

    private GatewayDTO nuevoGateway(String nombre, Gateway.TipoGateway tipo) {
        GatewayDTO g = new GatewayDTO();
        g.setNombre(nombre);
        g.setTipo(tipo);
        return g;
    }

    private void autenticarComoAdmin(Long empresaId) {
        UsuarioPrincipal admin = new UsuarioPrincipal(1L, empresaId, Usuario.RolAcceso.ADMIN, "admin@prueba.com");
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(admin, null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));
    }
}

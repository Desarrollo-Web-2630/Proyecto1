package com.proyecto1.thymeleaf.controllers;

import com.proyecto1.thymeleaf.model.Actividad;
import com.proyecto1.thymeleaf.model.Empresa;
import com.proyecto1.thymeleaf.model.Gateway;
import com.proyecto1.thymeleaf.model.Proceso;
import com.proyecto1.thymeleaf.model.Usuario;
import com.proyecto1.thymeleaf.repository.EmpresaRepository;
import com.proyecto1.thymeleaf.services.ActividadService;
import com.proyecto1.thymeleaf.services.GatewayService;
import com.proyecto1.thymeleaf.services.ProcesoService;
import com.proyecto1.thymeleaf.services.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Smoke test de la capa de vistas: recorre cada pantalla GET y comprueba que
 * la plantilla Thymeleaf existe y renderiza sin reventar.
 *
 * Los errores de Thymeleaf no aparecen al compilar, solo al renderizar, asi
 * que sin esta prueba una plantilla rota pasa desapercibida hasta produccion.
 */
@SpringBootTest
@AutoConfigureMockMvc
class VistasRenderizanTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EmpresaRepository empresaRepository;

    @Autowired
    private ProcesoService procesoService;

    @Autowired
    private ActividadService actividadService;

    @Autowired
    private GatewayService gatewayService;

    @Autowired
    private UsuarioService usuarioService;

    // Los controladores usan EMPRESA_ID_MOCK = 1L mientras no exista Spring Security
    private static final Long EMPRESA_ID = 1L;

    private Long procesoId;
    private Long actividadId;
    private Long gatewayId;

    @BeforeEach
    void prepararDatos() {
        if (empresaRepository.findById(EMPRESA_ID).isEmpty()) {
            Empresa empresa = new Empresa();
            empresa.setNombre("Empresa de prueba");
            empresa.setNit("900111222");
            empresa.setCorreo("contacto@prueba.com");
            empresaRepository.save(empresa);
        }

        Proceso proceso = new Proceso();
        proceso.setNombre("Proceso " + System.nanoTime());
        proceso.setDescripcion("Proceso de prueba");
        proceso.setCategoria("Operaciones");
        procesoId = procesoService.crearProceso(proceso, EMPRESA_ID).getId();

        Actividad actividad = new Actividad();
        actividad.setNombre("Revisar solicitud");
        actividad.setTipoActividad("TAREA_USUARIO");
        actividad.setPosicionX(10);
        actividad.setPosicionY(20);
        actividad.setLaneId(1L);
        actividadId = actividadService.crearActividad(actividad, procesoId, EMPRESA_ID).getId();

        Gateway gateway = new Gateway();
        gateway.setNombre("Aprobada?");
        gateway.setTipo(Gateway.TipoGateway.EXCLUSIVO);
        gatewayId = gatewayService.crearGateway(gateway, procesoId, EMPRESA_ID).getId();

        Usuario usuario = new Usuario();
        usuario.setNombre("Ana Martinez");
        usuario.setCorreo("ana" + System.nanoTime() + "@prueba.com");
        usuario.setPassword("secreta");
        usuario.setRolAcceso(Usuario.RolAcceso.ADMIN);
        usuarioService.registrarUsuario(usuario, EMPRESA_ID);
    }

    @Test
    void lasPantallasDeProcesosRenderizan() throws Exception {
        mockMvc.perform(get("/procesos")).andExpect(status().isOk());
        mockMvc.perform(get("/procesos/nuevo")).andExpect(status().isOk());
        mockMvc.perform(get("/procesos/{id}", procesoId)).andExpect(status().isOk());
        mockMvc.perform(get("/procesos/editar/{id}", procesoId)).andExpect(status().isOk());
    }

    @Test
    void lasPantallasDeActividadesRenderizan() throws Exception {
        mockMvc.perform(get("/procesos/{p}/actividades", procesoId)).andExpect(status().isOk());
        mockMvc.perform(get("/procesos/{p}/actividades/nueva", procesoId)).andExpect(status().isOk());
        mockMvc.perform(get("/procesos/{p}/actividades/editar/{id}", procesoId, actividadId))
                .andExpect(status().isOk());
    }

    @Test
    void lasPantallasDeGatewaysRenderizan() throws Exception {
        mockMvc.perform(get("/procesos/{p}/gateways", procesoId)).andExpect(status().isOk());
        mockMvc.perform(get("/procesos/{p}/gateways/nuevo", procesoId)).andExpect(status().isOk());
        mockMvc.perform(get("/procesos/{p}/gateways/editar/{id}", procesoId, gatewayId))
                .andExpect(status().isOk());
    }

    @Test
    void lasPantallasDeArcosRenderizan() throws Exception {
        mockMvc.perform(get("/procesos/{p}/arcos", procesoId)).andExpect(status().isOk());
        mockMvc.perform(get("/procesos/{p}/arcos/nuevo", procesoId)).andExpect(status().isOk());
    }

    @Test
    void lasPantallasDeEmpresasRenderizan() throws Exception {
        mockMvc.perform(get("/empresas")).andExpect(status().isOk());
        mockMvc.perform(get("/empresas/nueva")).andExpect(status().isOk());
        mockMvc.perform(get("/empresas/{id}", EMPRESA_ID)).andExpect(status().isOk());
        mockMvc.perform(get("/empresas/editar/{id}", EMPRESA_ID)).andExpect(status().isOk());
    }

    @Test
    void lasPantallasDeUsuariosRenderizan() throws Exception {
        mockMvc.perform(get("/usuarios")).andExpect(status().isOk());
        mockMvc.perform(get("/usuarios/nuevo")).andExpect(status().isOk());
        mockMvc.perform(get("/usuarios/login")).andExpect(status().isOk());
    }
}

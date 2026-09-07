package com.proyecto1.thymeleaf.controllers;

import com.proyecto1.thymeleaf.dto.ActividadDTO;
import com.proyecto1.thymeleaf.dto.EmpresaDTO;
import com.proyecto1.thymeleaf.dto.GatewayDTO;
import com.proyecto1.thymeleaf.dto.ProcesoDTO;
import com.proyecto1.thymeleaf.dto.UsuarioDTO;
import com.proyecto1.thymeleaf.model.Gateway;
import com.proyecto1.thymeleaf.model.Proceso;
import com.proyecto1.thymeleaf.model.Usuario;
import com.proyecto1.thymeleaf.repository.EmpresaRepository;
import com.proyecto1.thymeleaf.services.ActividadService;
import com.proyecto1.thymeleaf.services.EmpresaService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

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
    private EmpresaService empresaService;

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
            EmpresaDTO empresa = new EmpresaDTO();
            empresa.setNombre("Empresa de prueba");
            empresa.setNit("900111222");
            empresa.setCorreo("contacto@prueba.com");
            empresaService.registrarEmpresa(empresa);
        }

        ProcesoDTO proceso = new ProcesoDTO();
        proceso.setNombre("Proceso " + System.nanoTime());
        proceso.setDescripcion("Proceso de prueba");
        proceso.setCategoria("Operaciones");
        procesoId = procesoService.crearProceso(proceso, EMPRESA_ID).getId();

        ActividadDTO actividad = new ActividadDTO();
        actividad.setNombre("Revisar solicitud");
        actividad.setTipoActividad("TAREA_USUARIO");
        actividad.setPosicionX(10);
        actividad.setPosicionY(20);
        actividad.setLaneId(1L);
        actividadId = actividadService.crearActividad(actividad, procesoId, EMPRESA_ID).getId();

        GatewayDTO gateway = new GatewayDTO();
        gateway.setNombre("Aprobada?");
        gateway.setTipo(Gateway.TipoGateway.EXCLUSIVO);
        gatewayId = gatewayService.crearGateway(gateway, procesoId, EMPRESA_ID).getId();

        UsuarioDTO usuario = new UsuarioDTO();
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

    // ---------- Validacion con BindingResult ----------

    @Test
    void elFormularioDeProcesoVuelveConLosErroresCuandoFaltanDatos() throws Exception {
        mockMvc.perform(post("/procesos/guardar")
                        .param("nombre", "")
                        .param("descripcion", "")
                        .param("categoria", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("procesos/formulario"))
                .andExpect(model().attributeHasFieldErrors("proceso", "nombre", "descripcion", "categoria"));
    }

    @Test
    void elFormularioDeActividadRechazaUnaPosicionNegativa() throws Exception {
        mockMvc.perform(post("/procesos/{p}/actividades/guardar", procesoId)
                        .param("nombre", "Otra actividad")
                        .param("tipoActividad", "TAREA_USUARIO")
                        .param("laneId", "1")
                        .param("posicionX", "-5")
                        .param("posicionY", "10"))
                .andExpect(status().isOk())
                .andExpect(view().name("actividades/formulario"))
                .andExpect(model().attributeHasFieldErrors("actividad", "posicionX"));
    }

    @Test
    void elNombreDuplicadoDeProcesoSeAvisaSinPerderElFormulario() throws Exception {
        Proceso existente = procesoService.obtenerPorIdYEmpresa(procesoId, EMPRESA_ID);

        mockMvc.perform(post("/procesos/guardar")
                        .param("nombre", existente.getNombre())
                        .param("descripcion", "Otra descripción")
                        .param("categoria", "Operaciones"))
                .andExpect(status().isOk())
                .andExpect(view().name("procesos/formulario"))
                .andExpect(model().attributeExists("mensajeError"));
    }
}

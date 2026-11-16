package com.proyecto1.thymeleaf.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyecto1.thymeleaf.model.Empresa;
import com.proyecto1.thymeleaf.model.Proceso;
import com.proyecto1.thymeleaf.model.Usuario;
import com.proyecto1.thymeleaf.repository.EmpresaRepository;
import com.proyecto1.thymeleaf.repository.ProcesoRepository;
import com.proyecto1.thymeleaf.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Prueba de integracion: aplicacion completa con la cadena de filtros de Spring Security real
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("h2")
@Transactional
class SeguridadIntegracionTest {

    private static final String CLAVE = "Clave1234";

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private EmpresaRepository empresaRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private ProcesoRepository procesoRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private Empresa empresaA;
    private Proceso procesoA;

    @BeforeEach
    void prepararDatos() {
        empresaA = crearEmpresa("Empresa A", "900111111-1");
        Empresa empresaB = crearEmpresa("Empresa B", "900222222-2");
        crearUsuario("admin.a@test.com", Usuario.RolAcceso.ADMIN, empresaA, true);
        crearUsuario("editor.a@test.com", Usuario.RolAcceso.EDITOR, empresaA, true);
        crearUsuario("lector.a@test.com", Usuario.RolAcceso.LECTURA, empresaA, true);
        crearUsuario("inactivo.a@test.com", Usuario.RolAcceso.ADMIN, empresaA, false);
        crearUsuario("admin.b@test.com", Usuario.RolAcceso.ADMIN, empresaB, true);

        procesoA = new Proceso();
        procesoA.setNombre("Proceso secreto de A");
        procesoA.setDescripcion("Solo lo ve la empresa A");
        procesoA.setCategoria("RRHH");
        procesoA.setEmpresa(empresaA);
        procesoRepository.save(procesoA);
    }

    // ---- Autenticacion ----

    @Test
    void sinToken_responde401() throws Exception {
        mockMvc.perform(get("/api/v1/procesos"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensaje").value("Debe iniciar sesion para acceder a este recurso"));
    }

    @Test
    void tokenInventado_responde401() throws Exception {
        mockMvc.perform(get("/api/v1/procesos").header(HttpHeaders.AUTHORIZATION, "Bearer abc.def.ghi"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_correcto_devuelveTokenYDatosDelUsuario() throws Exception {
        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(credenciales("admin.a@test.com", CLAVE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.tipo").value("Bearer"))
                .andExpect(jsonPath("$.usuario.empresaId").value(empresaA.getId()))
                .andExpect(jsonPath("$.usuario.rolAcceso").value("ADMIN"))
                .andExpect(jsonPath("$.usuario.password").doesNotExist());
    }

    @Test
    void login_conClaveIncorrecta_responde401() throws Exception {
        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(credenciales("admin.a@test.com", "Otra12345")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.token").doesNotExist());
    }

    @Test
    void login_deUsuarioInactivo_responde401() throws Exception {
        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(credenciales("inactivo.a@test.com", CLAVE)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void yo_conToken_devuelveElUsuarioDelToken() throws Exception {
        mockMvc.perform(get("/api/auth/yo").header(HttpHeaders.AUTHORIZATION, bearer("editor.a@test.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correo").value("editor.a@test.com"))
                .andExpect(jsonPath("$.rolAcceso").value("EDITOR"));
    }

    @Test
    void usuarioDesactivadoDespuesDelLogin_pierdeElAcceso() throws Exception {
        String token = bearer("editor.a@test.com");
        Usuario editor = usuarioRepository.findByCorreoIgnoreCase("editor.a@test.com").orElseThrow();
        editor.setActivo(false);
        usuarioRepository.save(editor);

        mockMvc.perform(get("/api/v1/procesos").header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isUnauthorized());
    }

    // ---- Autorizacion por rol ----

    @Test
    void lectura_puedeConsultarProcesos() throws Exception {
        mockMvc.perform(get("/api/v1/procesos").header(HttpHeaders.AUTHORIZATION, bearer("lector.a@test.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].nombre", hasItem("Proceso secreto de A")));
    }

    @Test
    void lectura_noPuedeCrearProcesos_responde403() throws Exception {
        mockMvc.perform(post("/api/v1/procesos").header(HttpHeaders.AUTHORIZATION, bearer("lector.a@test.com"))
                        .contentType(MediaType.APPLICATION_JSON).content(procesoNuevo()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.mensaje").value("No tiene permisos para realizar esta accion"));
    }

    @Test
    void editor_puedeCrearProcesos_responde201() throws Exception {
        mockMvc.perform(post("/api/v1/procesos").header(HttpHeaders.AUTHORIZATION, bearer("editor.a@test.com"))
                        .contentType(MediaType.APPLICATION_JSON).content(procesoNuevo()))
                .andExpect(status().isCreated());
    }

    @Test
    void editor_noPuedeEliminarProcesos_responde403() throws Exception {
        mockMvc.perform(delete("/api/v1/procesos/" + procesoA.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer("editor.a@test.com")))
                .andExpect(status().isForbidden());
    }

    @Test
    void editor_noPuedeCrearUsuarios_responde403() throws Exception {
        mockMvc.perform(post("/api/v1/usuarios").header(HttpHeaders.AUTHORIZATION, bearer("editor.a@test.com"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Nuevo\",\"correo\":\"nuevo@test.com\",\"password\":\"Clave1234\",\"rolAcceso\":\"ADMIN\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void admin_puedeEliminarProcesos_responde204() throws Exception {
        mockMvc.perform(delete("/api/v1/procesos/" + procesoA.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer("admin.a@test.com")))
                .andExpect(status().isNoContent());
    }

    // ---- Aislamiento entre empresas ----

    @Test
    void adminDeOtraEmpresa_noVeElProcesoAjeno() throws Exception {
        mockMvc.perform(get("/api/v1/procesos/" + procesoA.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer("admin.b@test.com")))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void adminDeOtraEmpresa_noPuedeBorrarElProcesoAjeno() throws Exception {
        mockMvc.perform(delete("/api/v1/procesos/" + procesoA.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer("admin.b@test.com")))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void encabezadoXEmpresaIdFalso_seIgnora() throws Exception {
        // Antes la empresa salia de este encabezado; ahora solo cuenta la del token
        mockMvc.perform(get("/api/v1/procesos")
                        .header(HttpHeaders.AUTHORIZATION, bearer("admin.b@test.com"))
                        .header("X-Empresa-Id", String.valueOf(empresaA.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].nombre", everyItem(not("Proceso secreto de A"))));
    }

    @Test
    void adminDeOtraEmpresa_noPuedeVerLaEmpresaAjena_responde403() throws Exception {
        mockMvc.perform(get("/api/v1/empresas/" + empresaA.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer("admin.b@test.com")))
                .andExpect(status().isForbidden());
    }

    // ---- Rutas publicas ----

    @Test
    void registroDeEmpresa_esPublico() throws Exception {
        mockMvc.perform(post("/api/v1/empresas").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Empresa Nueva\",\"nit\":\"900333333-3\",\"correo\":\"nueva@test.com\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    void documentacionSwagger_esPublica() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk());
    }

    // ---- Utilidades ----

    private String bearer(String correo) throws Exception {
        String respuesta = mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(credenciales(correo, CLAVE)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode json = objectMapper.readTree(respuesta);
        return "Bearer " + json.get("token").asText();
    }

    private String credenciales(String correo, String clave) {
        return "{\"correo\":\"" + correo + "\",\"password\":\"" + clave + "\"}";
    }

    private String procesoNuevo() {
        return "{\"nombre\":\"Proceso nuevo\",\"descripcion\":\"Creado en la prueba\",\"categoria\":\"TI\"}";
    }

    private Empresa crearEmpresa(String nombre, String nit) {
        Empresa empresa = new Empresa();
        empresa.setNombre(nombre);
        empresa.setNit(nit);
        empresa.setCorreo(nit + "@test.com");
        return empresaRepository.save(empresa);
    }

    private void crearUsuario(String correo, Usuario.RolAcceso rol, Empresa empresa, boolean activo) {
        Usuario usuario = new Usuario();
        usuario.setNombre("Usuario " + rol);
        usuario.setCorreo(correo);
        usuario.setPassword(passwordEncoder.encode(CLAVE));
        usuario.setRolAcceso(rol);
        usuario.setActivo(activo);
        usuario.setEmpresa(empresa);
        usuarioRepository.save(usuario);
    }
}

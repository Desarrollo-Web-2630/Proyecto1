package com.proyecto1.thymeleaf.seguridad;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyecto1.thymeleaf.dto.ActivarCuentaRequestDTO;
import com.proyecto1.thymeleaf.dto.ActividadDTO;
import com.proyecto1.thymeleaf.dto.EmpresaDTO;
import com.proyecto1.thymeleaf.dto.LoginDTO;
import com.proyecto1.thymeleaf.dto.ProcesoDTO;
import com.proyecto1.thymeleaf.dto.UsuarioDTO;
import com.proyecto1.thymeleaf.model.Empresa;
import com.proyecto1.thymeleaf.model.Usuario;
import com.proyecto1.thymeleaf.model.VerificacionToken;
import com.proyecto1.thymeleaf.repository.UsuarioRepository;
import com.proyecto1.thymeleaf.repository.VerificacionTokenRepository;
import com.proyecto1.thymeleaf.services.EmpresaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Prueba de punta a punta, via HTTP real (MockMvc), de todo el flujo de
 * autenticacion: registrar empresa, activar cuenta, login, usar el JWT para
 * llamar un endpoint protegido, y el aislamiento entre empresas cuando el
 * token es de otro tenant.
 *
 * Se fuerza app.security.enabled=true para esta clase especificamente: el
 * resto de la suite corre con el perfil h2 (que lo trae en false, pensado
 * para pruebas de servicio que no pasan por HTTP), pero el proposito de esta
 * clase es exactamente verificar que la seguridad SI bloquea cuando esta
 * activada, que es como se comporta el despliegue real.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("h2")
@TestPropertySource(properties = "app.security.enabled=true")
@Transactional
class AutenticacionIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EmpresaService empresaService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private VerificacionTokenRepository tokenRepository;

    @Test
    void unaPeticionSinTokenAUnEndpointProtegidoRecibe401() throws Exception {
        mockMvc.perform(get("/api/v1/procesos"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void registrarEmpresaSiguePublicoSinToken() throws Exception {
        EmpresaDTO datos = new EmpresaDTO();
        datos.setNombre("Empresa HTTP");
        datos.setNit("900555666");
        datos.setCorreo("contacto@empresahttp.com");

        mockMvc.perform(post("/api/v1/empresas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(datos)))
                .andExpect(status().isCreated());
    }

    @Test
    void loginFallaConCredencialesIncorrectasConRespuestaGenerica() throws Exception {
        LoginDTO login = new LoginDTO();
        login.setCorreo("nadie@nunca-existio.com");
        login.setPassword("Cualquiera1");

        mockMvc.perform(post("/api/v1/usuarios/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("")); // no revela si el correo existe
    }

    @Test
    void flujoCompletoDeExtremoAExtremo() throws Exception {
        // 1. Registrar empresa (publico, sin token)
        Empresa empresa = registrarEmpresaHttp("Constructora HTTP", "900777888", "admin@constructorahttp.com");

        // 2. El admin nace inactivo: login todavia falla
        LoginDTO loginAntes = new LoginDTO();
        loginAntes.setCorreo(empresa.getCorreo());
        loginAntes.setPassword("Cualquiera1");
        mockMvc.perform(post("/api/v1/usuarios/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginAntes)))
                .andExpect(status().isUnauthorized());

        // 3. Activar la cuenta con el token que genero el registro
        Usuario admin = usuarioRepository.findByEmpresaId(empresa.getId()).get(0);
        VerificacionToken token = tokenRepository.findAll().stream()
                .filter(t -> t.getUsuario().getId().equals(admin.getId()))
                .findFirst().orElseThrow();

        ActivarCuentaRequestDTO activacion = new ActivarCuentaRequestDTO();
        activacion.setToken(token.getToken());
        activacion.setNuevaPassword("ClaveReal123");

        mockMvc.perform(post("/api/auth/activar-cuenta")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(activacion)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verificado").value(true));

        // 4. Login ahora si funciona y devuelve un JWT
        LoginDTO loginDespues = new LoginDTO();
        loginDespues.setCorreo(empresa.getCorreo());
        loginDespues.setPassword("ClaveReal123");

        String respuestaLogin = mockMvc.perform(post("/api/v1/usuarios/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDespues)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn().getResponse().getContentAsString();

        JsonNode json = objectMapper.readTree(respuestaLogin);
        String jwt = json.get("token").asText();

        // 5. El JWT sirve para crear un proceso en la propia empresa
        ProcesoDTO proceso = new ProcesoDTO();
        proceso.setNombre("Proceso creado por HTTP");
        proceso.setDescripcion("Prueba de extremo a extremo");
        proceso.setCategoria("Operaciones");

        mockMvc.perform(post("/api/v1/procesos")
                        .header("Authorization", "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(proceso)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("BORRADOR"));

        // 6. Listar procesos con el mismo JWT los devuelve
        mockMvc.perform(get("/api/v1/procesos").header("Authorization", "Bearer " + jwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Proceso creado por HTTP"));
    }

    @Test
    void unJwtDeUnaEmpresaNoVeLosProcesosDeOtra() throws Exception {
        String jwtEmpresaA = registrarActivarYObtenerToken("Empresa A HTTP", "900111000", "a@empresaahttp.com");
        String jwtEmpresaB = registrarActivarYObtenerToken("Empresa B HTTP", "900222000", "b@empresabhttp.com");

        ProcesoDTO proceso = new ProcesoDTO();
        proceso.setNombre("Proceso secreto de A");
        proceso.setDescripcion("Solo la empresa A debe verlo");
        proceso.setCategoria("Confidencial");

        mockMvc.perform(post("/api/v1/procesos")
                        .header("Authorization", "Bearer " + jwtEmpresaA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(proceso)))
                .andExpect(status().isCreated());

        // La empresa A si lo ve
        mockMvc.perform(get("/api/v1/procesos").header("Authorization", "Bearer " + jwtEmpresaA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Proceso secreto de A"));

        // La empresa B, con un JWT real pero de otro tenant, no ve nada
        mockMvc.perform(get("/api/v1/procesos").header("Authorization", "Bearer " + jwtEmpresaB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        assertNotEquals(jwtEmpresaA, jwtEmpresaB);
    }

    @Test
    void lasRespuestasNuncaExponenElHashDeLaContrasena() throws Exception {
        String jwt = registrarActivarYObtenerToken("Empresa Sin Fugas", "900444555", "admin@sinfugas.com");

        ProcesoDTO proceso = new ProcesoDTO();
        proceso.setNombre("Proceso a inspeccionar");
        proceso.setDescripcion("Se revisa el JSON completo");
        proceso.setCategoria("Auditoria");
        mockMvc.perform(post("/api/v1/procesos")
                        .header("Authorization", "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(proceso)))
                .andExpect(status().isCreated());

        // Proceso -> empresa -> usuarios -> password: si la entidad se serializa
        // completa, el hash BCrypt ("$2a$...") sale en la respuesta.
        String[] cuerpos = {
                mockMvc.perform(get("/api/v1/procesos").header("Authorization", "Bearer " + jwt))
                        .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(),
                mockMvc.perform(get("/api/v1/empresas").header("Authorization", "Bearer " + jwt))
                        .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(),
                mockMvc.perform(get("/api/v1/usuarios").header("Authorization", "Bearer " + jwt))
                        .andExpect(status().isOk()).andReturn().getResponse().getContentAsString()
        };
        for (String cuerpo : cuerpos) {
            org.junit.jupiter.api.Assertions.assertFalse(cuerpo.contains("\"password\""),
                    "La respuesta expone el campo password: " + cuerpo);
            org.junit.jupiter.api.Assertions.assertFalse(cuerpo.contains("$2a$") || cuerpo.contains("$2b$"),
                    "La respuesta expone un hash BCrypt: " + cuerpo);
        }
    }

    @Test
    void unBodyInvalidoDevuelve400ConMensaje() throws Exception {
        EmpresaDTO datos = new EmpresaDTO();
        datos.setNombre("X"); // menos de 2 caracteres
        datos.setNit("abc");  // no numerico
        datos.setCorreo("no-es-un-correo");

        mockMvc.perform(post("/api/v1/empresas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(datos)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.mensaje").isNotEmpty());
    }

    @Test
    void unTokenInventadoNoAutentica() throws Exception {
        mockMvc.perform(get("/api/v1/procesos").header("Authorization", "Bearer token.invalido.falso"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void unEditorNoPuedeEliminarActividades() throws Exception {
        // El admin crea el proceso y la actividad, e invita a un colega EDITOR
        String jwtAdmin = registrarActivarYObtenerToken("Empresa Roles HTTP", "900999000", "admin@empresaroles.com");

        ProcesoDTO proceso = new ProcesoDTO();
        proceso.setNombre("Proceso con roles");
        proceso.setDescripcion("Para probar permisos");
        proceso.setCategoria("RRHH");

        String respuestaProceso = mockMvc.perform(post("/api/v1/procesos")
                        .header("Authorization", "Bearer " + jwtAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(proceso)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long procesoId = objectMapper.readTree(respuestaProceso).get("id").asLong();

        ActividadDTO actividad = new ActividadDTO();
        actividad.setNombre("Revisar solicitud");
        actividad.setTipoActividad("TAREA_USUARIO");
        actividad.setLaneId(1L);
        actividad.setPosicionX(10);
        actividad.setPosicionY(10);

        String respuestaActividad = mockMvc.perform(post("/api/v1/procesos/{p}/actividades", procesoId)
                        .header("Authorization", "Bearer " + jwtAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(actividad)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long actividadId = objectMapper.readTree(respuestaActividad).get("id").asLong();

        // El admin invita a un colega EDITOR
        UsuarioDTO editorDatos = new UsuarioDTO();
        editorDatos.setNombre("Editor Colega");
        editorDatos.setCorreo("editor@empresaroles.com");
        editorDatos.setPassword("ClaveEditor1");
        editorDatos.setRolAcceso(Usuario.RolAcceso.EDITOR);

        mockMvc.perform(post("/api/v1/usuarios")
                        .header("Authorization", "Bearer " + jwtAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(editorDatos)))
                .andExpect(status().isCreated());

        // El colega verifica su correo (sin cambiar contrasena: el admin ya la puso)
        Usuario editor = usuarioRepository.findByCorreoIgnoreCase("editor@empresaroles.com").orElseThrow();
        VerificacionToken tokenEditor = tokenRepository.findAll().stream()
                .filter(t -> t.getUsuario().getId().equals(editor.getId()))
                .findFirst().orElseThrow();
        mockMvc.perform(get("/api/auth/verificar-correo").param("token", tokenEditor.getToken()))
                .andExpect(status().isOk());

        LoginDTO loginEditor = new LoginDTO();
        loginEditor.setCorreo("editor@empresaroles.com");
        loginEditor.setPassword("ClaveEditor1");
        String respuestaLoginEditor = mockMvc.perform(post("/api/v1/usuarios/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginEditor)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String jwtEditor = objectMapper.readTree(respuestaLoginEditor).get("token").asText();

        // El EDITOR puede ver la actividad...
        mockMvc.perform(get("/api/v1/procesos/{p}/actividades/{id}", procesoId, actividadId)
                        .header("Authorization", "Bearer " + jwtEditor))
                .andExpect(status().isOk());

        // ...pero no puede eliminarla: HU-10 la reserva al administrador
        mockMvc.perform(delete("/api/v1/procesos/{p}/actividades/{id}", procesoId, actividadId)
                        .header("Authorization", "Bearer " + jwtEditor))
                .andExpect(status().isForbidden());

        // El admin si puede
        mockMvc.perform(delete("/api/v1/procesos/{p}/actividades/{id}", procesoId, actividadId)
                        .header("Authorization", "Bearer " + jwtAdmin))
                .andExpect(status().isNoContent());
    }

    // ---------- utilidades ----------

    private Empresa registrarEmpresaHttp(String nombre, String nit, String correo) throws Exception {
        EmpresaDTO datos = new EmpresaDTO();
        datos.setNombre(nombre);
        datos.setNit(nit);
        datos.setCorreo(correo);

        return empresaService.registrarEmpresa(datos);
    }

    /**
     * Registra una empresa, activa su administrador con el token real
     * generado por el registro, e inicia sesion. Devuelve el JWT listo para
     * usar en el header Authorization de otra llamada.
     */
    private String registrarActivarYObtenerToken(String nombre, String nit, String correo) throws Exception {
        Empresa empresa = registrarEmpresaHttp(nombre, nit, correo);
        Usuario admin = usuarioRepository.findByEmpresaId(empresa.getId()).get(0);
        VerificacionToken token = tokenRepository.findAll().stream()
                .filter(t -> t.getUsuario().getId().equals(admin.getId()))
                .findFirst().orElseThrow();

        ActivarCuentaRequestDTO activacion = new ActivarCuentaRequestDTO();
        activacion.setToken(token.getToken());
        activacion.setNuevaPassword("ClaveReal123");

        mockMvc.perform(post("/api/auth/activar-cuenta")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(activacion)))
                .andExpect(status().isOk());

        LoginDTO login = new LoginDTO();
        login.setCorreo(correo);
        login.setPassword("ClaveReal123");

        String respuesta = mockMvc.perform(post("/api/v1/usuarios/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(respuesta).get("token").asText();
    }
}

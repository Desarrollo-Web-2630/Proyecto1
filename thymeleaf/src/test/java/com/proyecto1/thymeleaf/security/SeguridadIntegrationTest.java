package com.proyecto1.thymeleaf.security;

import com.jayway.jsonpath.JsonPath;
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
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Pruebas de integracion de la seguridad: contexto completo de Spring, filtro de Spring Security
// real, H2 en memoria y tokens emitidos por el endpoint de login.
// Cubren autenticacion (login, token ausente/alterado/vencido), autorizacion por rol,
// aislamiento entre empresas y CORS.
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("h2")
@TestPropertySource(properties = {
        "app.demo.admin-password=" + SeguridadIntegrationTest.CLAVE_ADMIN_DEMO,
        "app.jwt.secret=clave-de-pruebas-de-integracion-con-mas-de-32-caracteres",
        "app.mail.enabled=false",
        "app.cors.origenes=http://localhost:4200"
})
class SeguridadIntegrationTest {

    static final String CLAVE_ADMIN_DEMO = "Admin12345";
    private static final String CLAVE = "Clave12345";
    private static final String NIT_DEMO = "900123456-7";
    private static final String NIT_OTRA = "800111222-3";
    private static final String PROCESO_OTRA = "Proceso confidencial de Otra SAS";
    private static final String PROCESOS = "/api/v1/procesos";

    @Autowired private MockMvc mockMvc;
    @Autowired private EmpresaRepository empresaRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private ProcesoRepository procesoRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtEncoder jwtEncoder;

    private Empresa demo;
    private Empresa otra;

    // Empresa Demo (DataInitializer) con admin, editor y lector; Otra SAS con su admin y un proceso
    @BeforeEach
    void prepararDatos() {
        demo = empresaPorNit(NIT_DEMO);
        if (!empresaRepository.existsByNit(NIT_OTRA)) {
            Empresa nueva = new Empresa();
            nueva.setNombre("Otra SAS");
            nueva.setNit(NIT_OTRA);
            nueva.setCorreo("contacto@otra.com");
            empresaRepository.save(nueva);
        }
        otra = empresaPorNit(NIT_OTRA);

        crearUsuarioSiFalta("editor@demo.com", Usuario.RolAcceso.EDITOR, demo);
        crearUsuarioSiFalta("lector@demo.com", Usuario.RolAcceso.LECTURA, demo);
        crearUsuarioSiFalta("admin@otra.com", Usuario.RolAcceso.ADMIN, otra);

        if (procesoRepository.findByEmpresaId(otra.getId()).isEmpty()) {
            Proceso proceso = new Proceso();
            proceso.setNombre(PROCESO_OTRA);
            proceso.setDescripcion("No debe verlo ninguna otra empresa");
            proceso.setCategoria("Finanzas");
            proceso.setEmpresa(otra);
            procesoRepository.save(proceso);
        }
    }

    // ---------- Autenticacion ----------

    @Test
    void login_conCredencialesCorrectas_devuelveTokenYUsuarioSinContrasena() throws Exception {
        mockMvc.perform(post("/api/v1/usuarios/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(credenciales("admin@demo.com", CLAVE_ADMIN_DEMO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", not(emptyOrNullString())))
                .andExpect(jsonPath("$.tipo").value("Bearer"))
                .andExpect(jsonPath("$.usuario.correo").value("admin@demo.com"))
                .andExpect(jsonPath("$.usuario.rolAcceso").value("ADMIN"))
                .andExpect(jsonPath("$.usuario.password").doesNotExist());
    }

    @Test
    void login_conClaveIncorrecta_devuelve401ConMensajeGenerico() throws Exception {
        mockMvc.perform(post("/api/v1/usuarios/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(credenciales("admin@demo.com", "Incorrecta123")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensaje").value("Correo o contrasena incorrectos"));
    }

    @Test
    void login_conCorreoInexistente_devuelveElMismo401() throws Exception {
        mockMvc.perform(post("/api/v1/usuarios/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(credenciales("nadie@ninguna.com", "Cualquiera123")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensaje").value("Correo o contrasena incorrectos"));
    }

    @Test
    void login_conUnTokenViejoEnElEncabezado_igualFunciona() throws Exception {
        mockMvc.perform(post("/api/v1/usuarios/login")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer token.viejo.invalido")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(credenciales("admin@demo.com", CLAVE_ADMIN_DEMO)))
                .andExpect(status().isOk());
    }

    @Test
    void rutaPrivada_sinToken_devuelve401() throws Exception {
        mockMvc.perform(get(PROCESOS))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE, containsString("Bearer")))
                .andExpect(jsonPath("$.mensaje").value(ManejadorErroresSeguridad.SIN_TOKEN));
    }

    @Test
    void rutaPrivada_conTokenAlterado_devuelve401() throws Exception {
        String token = login("admin@demo.com", CLAVE_ADMIN_DEMO);
        int firma = token.lastIndexOf('.') + 1;
        String alterado = token.substring(0, firma) + (token.charAt(firma) == 'A' ? 'B' : 'A') + token.substring(firma + 1);

        mockMvc.perform(get(PROCESOS).header(HttpHeaders.AUTHORIZATION, "Bearer " + alterado))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rutaPrivada_conTokenVencido_devuelve401() throws Exception {
        Instant hace2Horas = Instant.now().minus(2, ChronoUnit.HOURS);
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject("admin@demo.com")
                .issuedAt(hace2Horas)
                .expiresAt(hace2Horas.plus(1, ChronoUnit.HOURS))
                .claim(TokenService.CLAIM_USUARIO, 1L)
                .claim(TokenService.CLAIM_EMPRESA, demo.getId())
                .claim(TokenService.CLAIM_ROL, "ADMIN")
                .build();
        String vencido = jwtEncoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();

        mockMvc.perform(get(PROCESOS).header(HttpHeaders.AUTHORIZATION, "Bearer " + vencido))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensaje").value(ManejadorErroresSeguridad.TOKEN_INVALIDO));
    }

    @Test
    void registroDeEmpresa_esPublico() throws Exception {
        String nit = String.valueOf(ThreadLocalRandom.current().nextLong(100_000_000L, 999_999_999L));
        String cuerpo = """
                {"nombre": "Empresa Registro Prueba", "nit": "%s", "correo": "registro-%s@prueba.com"}
                """.formatted(nit, nit);

        mockMvc.perform(post("/api/v1/empresas").contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isCreated());
    }

    // ---------- Aislamiento entre empresas ----------

    @Test
    void listarProcesos_soloDevuelveLosDeLaEmpresaDelToken() throws Exception {
        mockMvc.perform(get(PROCESOS).header(HttpHeaders.AUTHORIZATION, bearer("admin@demo.com", CLAVE_ADMIN_DEMO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].nombre", hasItem("Solicitud de vacaciones")))
                .andExpect(jsonPath("$[*].nombre", not(hasItem(PROCESO_OTRA))));
    }

    @Test
    void procesoDeOtraEmpresa_devuelve404() throws Exception {
        Long idOtra = procesoRepository.findByEmpresaId(otra.getId()).get(0).getId();

        mockMvc.perform(get(PROCESOS + "/" + idOtra).header(HttpHeaders.AUTHORIZATION, bearer("admin@demo.com", CLAVE_ADMIN_DEMO)))
                .andExpect(status().isNotFound());
    }

    @Test
    void elementosDeUnProcesoDeOtraEmpresa_devuelven404() throws Exception {
        Long idOtra = procesoRepository.findByEmpresaId(otra.getId()).get(0).getId();

        mockMvc.perform(get(PROCESOS + "/" + idOtra + "/pools").header(HttpHeaders.AUTHORIZATION, bearer("admin@demo.com", CLAVE_ADMIN_DEMO)))
                .andExpect(status().isNotFound());
    }

    @Test
    void encabezadoXEmpresaId_seIgnora_laEmpresaSaleDelToken() throws Exception {
        mockMvc.perform(get(PROCESOS)
                        .header(HttpHeaders.AUTHORIZATION, bearer("admin@otra.com", CLAVE))
                        .header("X-Empresa-Id", demo.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].nombre", hasItem(PROCESO_OTRA)))
                .andExpect(jsonPath("$[*].nombre", not(hasItem("Solicitud de vacaciones"))));
    }

    @Test
    void empresas_soloDevuelveLaPropia_yOtraEmpresaEs404() throws Exception {
        String token = bearer("admin@demo.com", CLAVE_ADMIN_DEMO);

        mockMvc.perform(get("/api/v1/empresas").header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nit").value(NIT_DEMO));

        mockMvc.perform(get("/api/v1/empresas/" + otra.getId()).header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isNotFound());
    }

    @Test
    void usuarios_soloListaLosDeLaMismaEmpresa() throws Exception {
        mockMvc.perform(get("/api/v1/usuarios").header(HttpHeaders.AUTHORIZATION, bearer("admin@otra.com", CLAVE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].correo", hasItem("admin@otra.com")))
                .andExpect(jsonPath("$[*].correo", not(hasItem("admin@demo.com"))));
    }

    // ---------- Autorizacion por rol ----------

    @Test
    void lectura_puedeConsultar() throws Exception {
        mockMvc.perform(get(PROCESOS).header(HttpHeaders.AUTHORIZATION, bearer("lector@demo.com", CLAVE)))
                .andExpect(status().isOk());
    }

    @Test
    void lectura_noPuedeCrearProcesos_403() throws Exception {
        mockMvc.perform(post(PROCESOS)
                        .header(HttpHeaders.AUTHORIZATION, bearer("lector@demo.com", CLAVE))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(procesoNuevo()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.mensaje").value(ManejadorErroresSeguridad.SIN_PERMISO));
    }

    @Test
    void editor_puedeCrearProcesosEnSuEmpresa() throws Exception {
        mockMvc.perform(post(PROCESOS)
                        .header(HttpHeaders.AUTHORIZATION, bearer("editor@demo.com", CLAVE))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(procesoNuevo()))
                .andExpect(status().isCreated());
    }

    @Test
    void editor_noPuedeEliminarProcesos_403() throws Exception {
        Long idDemo = procesoRepository.findByEmpresaId(demo.getId()).get(0).getId();

        mockMvc.perform(delete(PROCESOS + "/" + idDemo).header(HttpHeaders.AUTHORIZATION, bearer("editor@demo.com", CLAVE)))
                .andExpect(status().isForbidden());
    }

    @Test
    void editor_noPuedeInactivarProcesos_403() throws Exception {
        Long idDemo = procesoRepository.findByEmpresaId(demo.getId()).get(0).getId();

        mockMvc.perform(post(PROCESOS + "/" + idDemo + "/inactivar").header(HttpHeaders.AUTHORIZATION, bearer("editor@demo.com", CLAVE)))
                .andExpect(status().isForbidden());
    }

    @Test
    void editor_noPuedeInvitarUsuarios_403() throws Exception {
        mockMvc.perform(post("/api/v1/usuarios")
                        .header(HttpHeaders.AUTHORIZATION, bearer("editor@demo.com", CLAVE))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Intruso\",\"correo\":\"intruso@demo.com\",\"password\":\"Clave12345\",\"rolAcceso\":\"ADMIN\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void editor_noPuedeCrearRolesDeProceso_403() throws Exception {
        mockMvc.perform(post("/api/v1/roles-proceso")
                        .header(HttpHeaders.AUTHORIZATION, bearer("editor@demo.com", CLAVE))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Auditor\",\"descripcion\":\"Revisa\"}"))
                .andExpect(status().isForbidden());
    }

    // ---------- CORS ----------

    @Test
    void cors_origenDelFront_esAceptado() throws Exception {
        mockMvc.perform(options(PROCESOS)
                        .header(HttpHeaders.ORIGIN, "http://localhost:4200")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "Authorization"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:4200"));
    }

    @Test
    void cors_origenDesconocido_esRechazado() throws Exception {
        mockMvc.perform(options(PROCESOS)
                        .header(HttpHeaders.ORIGIN, "https://sitio-malicioso.example")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"))
                .andExpect(status().isForbidden());
    }

    // ---------- Utilidades ----------

    private String bearer(String correo, String clave) throws Exception {
        return "Bearer " + login(correo, clave);
    }

    private String login(String correo, String clave) throws Exception {
        String respuesta = mockMvc.perform(post("/api/v1/usuarios/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(credenciales(correo, clave)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(respuesta, "$.token");
    }

    private static String credenciales(String correo, String clave) {
        return "{\"correo\":\"" + correo + "\",\"password\":\"" + clave + "\"}";
    }

    private static String procesoNuevo() {
        return "{\"nombre\":\"Proceso " + UUID.randomUUID() + "\",\"descripcion\":\"Creado en prueba\",\"categoria\":\"Pruebas\"}";
    }

    private Empresa empresaPorNit(String nit) {
        return empresaRepository.findAll().stream()
                .filter(e -> nit.equals(e.getNit()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No existe la empresa con NIT " + nit));
    }

    private void crearUsuarioSiFalta(String correo, Usuario.RolAcceso rol, Empresa empresa) {
        if (usuarioRepository.findByCorreoIgnoreCase(correo).isPresent()) {
            return;
        }
        Usuario usuario = new Usuario();
        usuario.setNombre("Usuario " + rol.name());
        usuario.setCorreo(correo);
        usuario.setPassword(passwordEncoder.encode(CLAVE));
        usuario.setRolAcceso(rol);
        usuario.setActivo(true);
        usuario.setEmpresa(empresa);
        usuarioRepository.save(usuario);
    }
}

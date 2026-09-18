package com.proyecto1.thymeleaf.services;

import com.proyecto1.thymeleaf.dto.EmpresaDTO;
import com.proyecto1.thymeleaf.dto.UsuarioDTO;
import com.proyecto1.thymeleaf.dto.VerificacionCorreoResponseDTO;
import com.proyecto1.thymeleaf.model.Empresa;
import com.proyecto1.thymeleaf.model.Usuario;
import com.proyecto1.thymeleaf.model.VerificacionToken;
import com.proyecto1.thymeleaf.repository.UsuarioRepository;
import com.proyecto1.thymeleaf.repository.VerificacionTokenRepository;
import com.proyecto1.thymeleaf.security.UsuarioPrincipal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Prueba el flujo completo de verificacion de cuentas (HU-01, HU-02, HU-03):
 * registro deja al usuario inactivo, login lo rechaza mientras tanto, y el
 * token de activacion se acepta, rechaza por invalido, por expirado y por ya
 * usado, cada uno por separado.
 */
@SpringBootTest
@ActiveProfiles("h2")
@Transactional
class VerificacionCorreoServiceTest {

    @Autowired
    private EmpresaService empresaService;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private VerificacionCorreoService verificacionCorreoService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private VerificacionTokenRepository tokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @AfterEach
    void limpiarContextoDeSeguridad() {
        // SecurityContextHolder usa un ThreadLocal; sin esto, un test que
        // autentica a alguien "contamina" al siguiente si JUnit reutiliza el
        // hilo.
        SecurityContextHolder.clearContext();
    }

    // ---------- HU-01: administrador inicial ----------

    @Test
    void elAdministradorInicialNaceInactivoYSinContrasenaConocida() {
        Empresa empresa = registrarEmpresaDePrueba("Constructora Norte", "900123456");

        Usuario admin = usuarioRepository.findByEmpresaId(empresa.getId()).get(0);

        assertFalse(admin.getActivo(), "El administrador debe nacer inactivo hasta verificar el correo");
        // La contrasena hardcodeada de antes era literalmente "DesarrolloWeb123"
        assertFalse(passwordEncoder.matches("DesarrolloWeb123", admin.getPassword()),
                "No debe quedar ninguna contrasena fija ni predecible");
    }

    @Test
    void noSePuedeIniciarSesionAntesDeActivarLaCuenta() {
        registrarEmpresaDePrueba("Empresa Pendiente", String.format("900%06d", System.nanoTime() % 1000000));
        Usuario admin = usuarioRepository.findAll().stream()
                .filter(u -> u.getRolAcceso() == Usuario.RolAcceso.ADMIN)
                .reduce((first, second) -> second) // el mas reciente
                .orElseThrow();

        assertTrue(usuarioService.login(admin.getCorreo(), "loQueSea1").isEmpty());
    }

    @Test
    void activarCuentaConTokenValidoPermiteIniciarSesionDespues() {
        Empresa empresa = registrarEmpresaDePrueba("Empresa Activable", String.format("900%06d", System.nanoTime() % 1000000));
        Usuario admin = usuarioRepository.findByEmpresaId(empresa.getId()).get(0);
        VerificacionToken token = tokenRepository.findAll().stream()
                .filter(t -> t.getUsuario().getId().equals(admin.getId()))
                .findFirst().orElseThrow();

        VerificacionCorreoResponseDTO respuesta = verificacionCorreoService.activarCuenta(token.getToken(), "ClaveSegura1");

        assertTrue(respuesta.isVerificado());
        assertTrue(usuarioService.login(admin.getCorreo(), "ClaveSegura1").isPresent());
    }

    @Test
    void activarCuentaRechazaUnaContrasenaDebil() {
        Empresa empresa = registrarEmpresaDePrueba("Empresa Debil", String.format("900%06d", System.nanoTime() % 1000000));
        Usuario admin = usuarioRepository.findByEmpresaId(empresa.getId()).get(0);
        VerificacionToken token = tokenRepository.findAll().stream()
                .filter(t -> t.getUsuario().getId().equals(admin.getId()))
                .findFirst().orElseThrow();

        VerificacionCorreoResponseDTO respuesta = verificacionCorreoService.activarCuenta(token.getToken(), "123");

        assertFalse(respuesta.isVerificado());
        assertTrue(usuarioService.login(admin.getCorreo(), "123").isEmpty());
    }

    // ---------- Token invalido, expirado, usado ----------

    @Test
    void unTokenInexistenteEsRechazado() {
        VerificacionCorreoResponseDTO respuesta = verificacionCorreoService.verificarCorreo("token-que-no-existe");

        assertFalse(respuesta.isVerificado());
        assertEquals("Token no encontrado", respuesta.getMensaje());
    }

    @Test
    void unTokenExpiradoEsRechazado() {
        Usuario usuario = crearUsuarioSuelto();
        VerificacionToken expirado = new VerificacionToken("tok-expirado-" + System.nanoTime(), usuario,
                Instant.now().minusSeconds(60));
        tokenRepository.save(expirado);

        VerificacionCorreoResponseDTO respuesta = verificacionCorreoService.verificarCorreo(expirado.getToken());

        assertFalse(respuesta.isVerificado());
        assertEquals("Token expirado", respuesta.getMensaje());
    }

    @Test
    void unTokenYaUsadoEsRechazado() {
        Usuario usuario = crearUsuarioSuelto();
        VerificacionToken vigente = new VerificacionToken("tok-usado-" + System.nanoTime(), usuario,
                Instant.now().plusSeconds(3600));
        vigente.setUsado(true);
        tokenRepository.save(vigente);

        VerificacionCorreoResponseDTO respuesta = verificacionCorreoService.verificarCorreo(vigente.getToken());

        assertFalse(respuesta.isVerificado());
        assertEquals("Token ya utilizado", respuesta.getMensaje());
    }

    @Test
    void unTokenVigenteActivaLaCuenta() {
        Usuario usuario = crearUsuarioSuelto();
        VerificacionToken vigente = new VerificacionToken("tok-vigente-" + System.nanoTime(), usuario,
                Instant.now().plusSeconds(3600));
        tokenRepository.save(vigente);

        VerificacionCorreoResponseDTO respuesta = verificacionCorreoService.verificarCorreo(vigente.getToken());

        assertTrue(respuesta.isVerificado());
        assertTrue(usuarioRepository.findById(usuario.getId()).orElseThrow().getActivo());
    }

    // ---------- HU-02: reenvio de verificacion ----------

    @Test
    void reenviarVerificacionGeneraUnTokenNuevo() {
        Usuario usuario = crearUsuarioSuelto();
        long tokensAntes = tokenRepository.count();

        verificacionCorreoService.reenviarVerificacion(usuario.getCorreo());

        assertEquals(tokensAntes + 1, tokenRepository.count());
    }

    // ---------- utilidades ----------

    private Empresa registrarEmpresaDePrueba(String nombre, String nit) {
        EmpresaDTO datos = new EmpresaDTO();
        datos.setNombre(nombre);
        datos.setNit(nit);
        datos.setCorreo("contacto" + System.nanoTime() + "@" + nit + ".com");
        return empresaService.registrarEmpresa(datos);
    }

    /**
     * Un usuario ya activo y con contrasena real, para probar el endpoint de
     * verificacion simple (sin cambio de contrasena) de forma aislada.
     *
     * registrarUsuario ahora exige que quien invita sea un administrador
     * autenticado, asi que este helper simula esa autenticacion antes de
     * llamarlo: es exactamente el mismo camino que seguiria una peticion HTTP
     * real con un JWT de administrador.
     */
    private Usuario crearUsuarioSuelto() {
        Empresa empresa = registrarEmpresaDePrueba("Empresa Base " + System.nanoTime(),
                String.valueOf(900000000L + (System.nanoTime() % 90000000L)));

        autenticarComo(empresa.getId(), Usuario.RolAcceso.ADMIN);

        UsuarioDTO datos = new UsuarioDTO();
        datos.setNombre("Usuario de prueba");
        datos.setCorreo("usuario" + System.nanoTime() + "@prueba.com");
        datos.setPassword("ClaveSegura1");
        datos.setRolAcceso(Usuario.RolAcceso.EDITOR);

        return usuarioService.registrarUsuario(datos, empresa.getId());
    }

    private void autenticarComo(Long empresaId, Usuario.RolAcceso rol) {
        UsuarioPrincipal principal = new UsuarioPrincipal(999L, empresaId, rol, "test@prueba.com");
        var authentication = new UsernamePasswordAuthenticationToken(
                principal, null, List.of(new SimpleGrantedAuthority("ROLE_" + rol.name())));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}

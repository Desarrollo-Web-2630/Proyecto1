package com.proyecto1.thymeleaf.e2e;

import com.proyecto1.thymeleaf.model.Empresa;
import com.proyecto1.thymeleaf.model.Proceso;
import com.proyecto1.thymeleaf.model.RolProceso;
import com.proyecto1.thymeleaf.model.Usuario;
import com.proyecto1.thymeleaf.repository.EmpresaRepository;
import com.proyecto1.thymeleaf.repository.ProcesoRepository;
import com.proyecto1.thymeleaf.repository.RolProcesoRepository;
import com.proyecto1.thymeleaf.repository.UsuarioRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Base de las pruebas de sistema (E2E).
 *
 * La prueba arranca el backend real en el puerto 8080 (perfil h2, misma cadena de
 * Spring Security que en produccion) y Selenium maneja Chrome contra el frontend
 * Angular, que debe estar corriendo en http://localhost:4200 (ng serve, cuyo proxy
 * envia /api al 8080). El flujo probado es: navegador -> Angular -> API REST -> JWT -> JPA.
 *
 * Se ejecutan con: mvnw test -Pe2e   (opciones: -De2e.headless=false -De2e.front=URL)
 */
@Tag("e2e")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, properties = {
        "server.port=8080",
        "app.demo.admin-password=" + E2EBase.CLAVE,
        "app.jwt.secret=clave-de-pruebas-e2e-con-mas-de-treinta-y-dos-caracteres",
        "app.cors.origenes=http://localhost:4200",
        "app.mail.enabled=false"
})
@ActiveProfiles("h2")
@ExtendWith(EvidenciaE2E.class)
abstract class E2EBase {

    static final String CLAVE = "Entrega2026*";
    static final String ADMIN = "admin@demo.com";
    static final String LECTOR = "lector@demo.com";
    static final String ADMIN_OTRA = "admin@otraempresa.com";
    static final String PROCESO_DEMO = "Solicitud de vacaciones";
    static final String PROCESO_OTRA = "Proceso privado de Otra Empresa";
    static final String ROL = "Analista de pruebas";
    static final String FRONT = System.getProperty("e2e.front", "http://localhost:4200");

    private static final String NIT_OTRA = "900999999-1";
    private static final AtomicInteger SECUENCIA = new AtomicInteger();

    @Autowired private EmpresaRepository empresaRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private RolProcesoRepository rolProcesoRepository;
    @Autowired protected ProcesoRepository procesoRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    protected WebDriver driver;
    protected WebDriverWait espera;

    @BeforeEach
    void prepararNavegadorYDatos() {
        prepararDatos();
        ChromeOptions opciones = new ChromeOptions();
        if (!"false".equalsIgnoreCase(System.getProperty("e2e.headless", "true"))) {
            opciones.addArguments("--headless=new");
        }
        opciones.addArguments("--window-size=1600,1000", "--lang=es");
        // Selenium Manager descarga el chromedriver que corresponde al Chrome instalado
        driver = new ChromeDriver(opciones);
        espera = new WebDriverWait(driver, Duration.ofSeconds(10));
        // Angular vuelve a pintar listas y nodos: un elemento viejo no es un fallo, se reintenta
        espera.ignoring(StaleElementReferenceException.class);
    }

    @AfterEach
    void cerrarNavegador() {
        if (driver != null) {
            driver.quit();
        }
    }

    // Datos semilla. Son idempotentes: la base H2 vive mientras dure la corrida.
    private void prepararDatos() {
        Empresa demo = empresaDe(ADMIN);
        if (!rolProcesoRepository.existsByNombreAndEmpresaId(ROL, demo.getId())) {
            RolProceso rol = new RolProceso();
            rol.setNombre(ROL);
            rol.setDescripcion("Rol para las lanes creadas en las pruebas E2E");
            rol.setEmpresa(demo);
            rolProcesoRepository.save(rol);
        }
        crearUsuarioSiFalta(LECTOR, "Lector Demo", Usuario.RolAcceso.LECTURA, demo);

        if (!empresaRepository.existsByNit(NIT_OTRA)) {
            Empresa otra = new Empresa();
            otra.setNombre("Otra Empresa SAS");
            otra.setNit(NIT_OTRA);
            otra.setCorreo("contacto@otraempresa.com");
            empresaRepository.save(otra);
            crearUsuarioSiFalta(ADMIN_OTRA, "Admin Otra", Usuario.RolAcceso.ADMIN, otra);
            crearProceso(PROCESO_OTRA, otra);
        }
    }

    private Empresa empresaDe(String correo) {
        Long empresaId = usuarioRepository.findByCorreoIgnoreCase(correo).orElseThrow().getEmpresa().getId();
        return empresaRepository.findById(empresaId).orElseThrow();
    }

    private void crearUsuarioSiFalta(String correo, String nombre, Usuario.RolAcceso rol, Empresa empresa) {
        if (usuarioRepository.existsByCorreoIgnoreCase(correo)) {
            return;
        }
        Usuario usuario = new Usuario();
        usuario.setNombre(nombre);
        usuario.setCorreo(correo);
        usuario.setPassword(passwordEncoder.encode(CLAVE));
        usuario.setRolAcceso(rol);
        usuario.setActivo(true);
        usuario.setEmpresa(empresa);
        usuarioRepository.save(usuario);
    }

    protected Proceso crearProceso(String nombre, Empresa empresa) {
        Proceso proceso = new Proceso();
        proceso.setNombre(nombre);
        proceso.setDescripcion("Creado por la prueba E2E");
        proceso.setCategoria("Pruebas");
        proceso.setEmpresa(empresa);
        return procesoRepository.save(proceso);
    }

    // Proceso vacio de la empresa demo, con nombre unico para no chocar entre pruebas
    protected Proceso procesoVacioDeDemo(String prefijo) {
        return crearProceso(nombreUnico(prefijo), empresaDe(ADMIN));
    }

    protected Long idDeProcesoDemo() {
        Long empresaId = empresaDe(ADMIN).getId();
        return procesoRepository.findByEmpresaId(empresaId).stream()
                .filter(p -> PROCESO_DEMO.equals(p.getNombre()))
                .findFirst().orElseThrow().getId();
    }

    protected static String nombreUnico(String prefijo) {
        return prefijo + " " + System.currentTimeMillis() % 100000 + "-" + SECUENCIA.incrementAndGet();
    }

    protected PaginaProcesos entrarComo(String correo) {
        return new PaginaLogin(driver, espera).abrir(FRONT).entrar(correo, CLAVE);
    }
}

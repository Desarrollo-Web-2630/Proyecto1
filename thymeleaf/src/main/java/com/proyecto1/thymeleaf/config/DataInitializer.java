package com.proyecto1.thymeleaf.config;

import com.proyecto1.thymeleaf.model.*;
import com.proyecto1.thymeleaf.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.SecureRandom;
import java.util.Base64;

// Inicializador de datos - crea empresa, admin y elementos si la BD está vacía.
// La contrasena del admin viene de DEMO_ADMIN_PASSWORD; si no esta definida, el
// admin queda inactivo con una contrasena aleatoria que nadie conoce (nunca una
// fija en el codigo) y se activa por el flujo normal de verificacion de correo.

@Configuration
public class DataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    // Reutilizado (S2119): crear SecureRandom en cada llamada es costoso
    // y desperdicia el pool de entropia.
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Value("${app.demo.admin-password:}")
    private String demoAdminPassword;

    public DataInitializer() {
    }

    @Bean
    CommandLineRunner cargarDatosBase(EmpresaRepository empresaRepo,
                                    UsuarioRepository usuarioRepo,
                                    ProcesoRepository procesoRepo,
                                    PasswordEncoder encoder) {
        return args -> {
            if (empresaRepo.count() > 0) return;

            Empresa empresa = new Empresa();
            empresa.setNombre("Empresa Demo");
            empresa.setNit("900123456-7");
            empresa.setCorreo("contacto@demo.com");
            empresaRepo.save(empresa);

            Usuario admin = new Usuario();
            admin.setNombre("Admin Demo");
            admin.setCorreo("admin@demo.com");
            boolean conPassword = demoAdminPassword != null && !demoAdminPassword.isBlank();
            admin.setPassword(encoder.encode(conPassword ? demoAdminPassword : passwordDescartable()));
            admin.setRolAcceso(Usuario.RolAcceso.ADMIN);
            admin.setActivo(conPassword);
            if (!conPassword) {
                log.warn("DEMO_ADMIN_PASSWORD no esta definida: admin@demo.com queda inactivo. "
                        + "Activalo con POST /api/auth/reenviar-verificacion y POST /api/auth/activar-cuenta.");
            }
            admin.setEmpresa(empresa);
            usuarioRepo.save(admin);

            Proceso proceso = new Proceso();
            proceso.setNombre("Solicitud de vacaciones");
            proceso.setDescripcion("Proceso demo de la primera entrega");
            proceso.setCategoria("RRHH");
            proceso.setEmpresa(empresa);
            procesoRepo.save(proceso);
        };
    }

    private static String passwordDescartable() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes);
    }
}
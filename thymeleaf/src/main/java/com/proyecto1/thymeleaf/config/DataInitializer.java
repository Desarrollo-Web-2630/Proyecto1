package com.proyecto1.thymeleaf.config;

import com.proyecto1.thymeleaf.model.*;
import com.proyecto1.thymeleaf.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Inicializador de datos - crea empresa, admin y elementos si la BD está vacía

@Configuration
public class DataInitializer {

    @Value("${app.demo.admin-password}")
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
            admin.setPassword(encoder.encode(demoAdminPassword));
            admin.setRolAcceso(Usuario.RolAcceso.ADMIN);
            admin.setActivo(true);
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
}
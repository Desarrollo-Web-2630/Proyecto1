package com.proyecto1.thymeleaf.config;


import com.proyecto1.thymeleaf.model.*;
import com.proyecto1.thymeleaf.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

// Inicializador de datos - crea empresa, admin y elementos si la BD está vacía
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
 
    private final EmpresaRepository empresaRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProcesoRepository procesoRepository;
    private final ActividadRepository actividadRepository;
    private final GatewayRepository gatewayRepository;
    private final ArcoRepository arcoRepository;
    private final PasswordEncoder passwordEncoder;
 
    @Override
    @Transactional
    public void run(String... args) {
        if (empresaRepository.count() > 0) {
            return;
        }
 
        Empresa empresa = new Empresa();
        empresa.setNombre("Empresa Demo S.A.S.");
        empresa.setNit("900123456-7");
        empresa.setCorreo("contacto@empresademo.com");
        empresa = empresaRepository.save(empresa);
 
        Usuario admin = new Usuario();
        admin.setNombre("Administrador Demo");
        admin.setCorreo("admin@empresademo.com");
        admin.setPassword(passwordEncoder.encode("Demo1234"));
        admin.setRolAcceso(Usuario.RolAcceso.ADMIN);
        admin.setEmpresa(empresa);
        usuarioRepository.save(admin);
 
        Proceso proceso = new Proceso();
        proceso.setNombre("Solicitud de vacaciones");
        proceso.setDescripcion("Proceso de ejemplo para validar herencia y cascada");
        proceso.setCategoria("Recursos Humanos");
        proceso.setEmpresa(empresa);
        proceso = procesoRepository.save(proceso);
 
        Actividad radicar = new Actividad();
        radicar.setNombre("Radicar solicitud");
        radicar.setTipoActividad("Manual");
        radicar.setLaneId(1L);
        radicar.setPosicionX(0);
        radicar.setPosicionY(0);
        radicar.setProceso(proceso);
        radicar = actividadRepository.save(radicar);
 
        Gateway decision = new Gateway();
        decision.setNombre("Aprobado?");
        decision.setTipo(Gateway.TipoGateway.EXCLUSIVO);
        decision.setPosicionX(200);
        decision.setPosicionY(0);
        decision.setProceso(proceso);
        decision = gatewayRepository.save(decision);
 
        // Este arco demuestra que la herencia quedo bien resuelta: origen
        // y destino son de subtipos distintos (Actividad y Gateway) pero
        // ambos son, en el fondo, un ElementoConectable.
        Arco arco = new Arco();
        arco.setNombre("Envio a revision");
        arco.setOrigen(radicar);
        arco.setDestino(decision);
        arco.setProceso(proceso);
        arcoRepository.save(arco);
    }
}

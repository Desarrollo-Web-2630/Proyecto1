package com.proyecto1.thymeleaf.config;

import com.proyecto1.thymeleaf.model.Empresa;
import com.proyecto1.thymeleaf.model.Proceso;
import com.proyecto1.thymeleaf.model.Usuario;
import com.proyecto1.thymeleaf.repository.EmpresaRepository;
import com.proyecto1.thymeleaf.repository.ProcesoRepository;
import com.proyecto1.thymeleaf.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DataInitializerTest {

    @Mock private EmpresaRepository empresaRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private ProcesoRepository procesoRepository;
    @Mock private PasswordEncoder passwordEncoder;

    @Test
    void cargarDatosBase_conBdVacia_creaEmpresaAdminYProceso() throws Exception {
        when(empresaRepository.count()).thenReturn(0L);
        when(passwordEncoder.encode(anyString())).thenReturn("hashDemo");

        DataInitializer dataInitializer = new DataInitializer();
        // demoAdminPassword lo inyecta Spring via @Value en tiempo real;
        // aqui lo seteamos a mano porque el test no levanta el ApplicationContext.
        ReflectionTestUtils.setField(dataInitializer, "demoAdminPassword", "Demo1234");

        CommandLineRunner runner = dataInitializer.cargarDatosBase(
                empresaRepository, usuarioRepository, procesoRepository, passwordEncoder);

        runner.run();

        verify(empresaRepository).save(argThat((Empresa e) -> "Empresa Demo".equals(e.getNombre())));
        verify(usuarioRepository).save(argThat((Usuario u) ->
                "admin@demo.com".equals(u.getCorreo()) && Boolean.TRUE.equals(u.getActivo())));
        verify(procesoRepository).save(argThat((Proceso p) -> "Solicitud de vacaciones".equals(p.getNombre())));
    }

    @Test
    void cargarDatosBase_conDatosExistentes_noCreaNada() throws Exception {
        when(empresaRepository.count()).thenReturn(1L);

        DataInitializer dataInitializer = new DataInitializer();
        ReflectionTestUtils.setField(dataInitializer, "demoAdminPassword", "Demo1234");

        CommandLineRunner runner = dataInitializer.cargarDatosBase(
                empresaRepository, usuarioRepository, procesoRepository, passwordEncoder);

        runner.run();

        verify(empresaRepository, never()).save(any());
        verify(usuarioRepository, never()).save(any());
        verify(procesoRepository, never()).save(any());
    }
}
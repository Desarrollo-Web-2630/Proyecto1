package com.proyecto1.thymeleaf.services;

import com.proyecto1.thymeleaf.model.Empresa;
import com.proyecto1.thymeleaf.dto.EmpresaDTO;
import com.proyecto1.thymeleaf.repository.EmpresaRepository;
import com.proyecto1.thymeleaf.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmpresaServiceTest {

    @Mock private EmpresaRepository empresaRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private VerificacionCorreoService verificacionCorreoService;

    @InjectMocks
    private EmpresaService empresaService;

    private EmpresaDTO datosValidos;

    @BeforeEach
    void setUp() {
        datosValidos = new EmpresaDTO();
        datosValidos.setNombre("Empresa Demo");
        datosValidos.setNit("900123456-7");
        datosValidos.setCorreo("contacto@demo.com");
    }

    @Test
    void listarTodas_devuelveLoQueRetornaElRepositorio() {
        when(empresaRepository.findAll()).thenReturn(List.of(new Empresa()));
        assertEquals(1, empresaService.listarTodas().size());
    }

    @Test
    void obtenerPorId_existente_devuelveLaEmpresa() {
        Empresa empresa = new Empresa();
        empresa.setId(1L);
        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));

        assertEquals(1L, empresaService.obtenerPorId(1L).getId());
    }

    @Test
    void obtenerPorId_inexistente_lanzaExcepcion() {
        when(empresaRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> empresaService.obtenerPorId(99L));
    }

    @Test
    void registrarEmpresa_conDatosValidos_creaEmpresaYAdminInactivo() {
        when(empresaRepository.existsByNit("900123456-7")).thenReturn(false);
        when(usuarioRepository.existsByCorreoIgnoreCase("contacto@demo.com")).thenReturn(false);
        when(empresaRepository.save(any(Empresa.class))).thenAnswer(inv -> inv.getArgument(0));
        when(usuarioRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(passwordEncoder.encode(anyString())).thenReturn("hashDescartable");

        Empresa resultado = empresaService.registrarEmpresa(datosValidos);

        assertEquals("Empresa Demo", resultado.getNombre());
        verify(verificacionCorreoService).crearYEnviarToken(any());
    }

    @Test
    void registrarEmpresa_conNitDuplicado_lanzaExcepcion() {
        when(empresaRepository.existsByNit("900123456-7")).thenReturn(true);
        assertThrows(IllegalArgumentException.class, () -> empresaService.registrarEmpresa(datosValidos));
    }

    @Test
    void registrarEmpresa_conCorreoYaUsadoPorUsuario_lanzaExcepcion() {
        when(empresaRepository.existsByNit("900123456-7")).thenReturn(false);
        when(usuarioRepository.existsByCorreoIgnoreCase("contacto@demo.com")).thenReturn(true);
        assertThrows(IllegalArgumentException.class, () -> empresaService.registrarEmpresa(datosValidos));
    }

    @Test
    void registrarEmpresa_conNombreVacio_lanzaExcepcion() {
        datosValidos.setNombre(" ");
        assertThrows(IllegalArgumentException.class, () -> empresaService.registrarEmpresa(datosValidos));
    }

    @Test
    void registrarEmpresa_conNitConFormatoInvalido_lanzaExcepcion() {
        datosValidos.setNit("abc");
        assertThrows(IllegalArgumentException.class, () -> empresaService.registrarEmpresa(datosValidos));
    }

    @Test
    void registrarEmpresa_conCorreoConFormatoInvalido_lanzaExcepcion() {
        datosValidos.setCorreo("no-es-correo");
        assertThrows(IllegalArgumentException.class, () -> empresaService.registrarEmpresa(datosValidos));
    }

    @Test
    void actualizarEmpresa_conDatosValidos_actualizaYGuarda() {
        Empresa existente = new Empresa();
        existente.setId(1L);
        existente.setNit("900123456-7");
        existente.setCorreo("contacto@demo.com");

        when(empresaRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(empresaRepository.save(any(Empresa.class))).thenAnswer(inv -> inv.getArgument(0));

        Empresa actualizado = empresaService.actualizarEmpresa(1L, datosValidos);

        assertEquals("Empresa Demo", actualizado.getNombre());
    }

    @Test
    void actualizarEmpresa_conNitYaUsadoPorOtra_lanzaExcepcion() {
        Empresa existente = new Empresa();
        existente.setId(1L);
        existente.setNit("999999999-9");
        existente.setCorreo("contacto@demo.com");

        when(empresaRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(empresaRepository.existsByNit("900123456-7")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> empresaService.actualizarEmpresa(1L, datosValidos));
    }

    @Test
    void eliminarEmpresa_existente_invocaDelete() {
        Empresa existente = new Empresa();
        existente.setId(1L);
        when(empresaRepository.findById(1L)).thenReturn(Optional.of(existente));

        empresaService.eliminarEmpresa(1L);

        verify(empresaRepository).delete(existente);
    }
}
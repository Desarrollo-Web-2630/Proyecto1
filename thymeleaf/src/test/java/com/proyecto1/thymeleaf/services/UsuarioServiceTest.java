package com.proyecto1.thymeleaf.services;

import com.proyecto1.thymeleaf.model.Empresa;
import com.proyecto1.thymeleaf.model.Usuario;
import com.proyecto1.thymeleaf.dto.UsuarioDTO;
import com.proyecto1.thymeleaf.repository.EmpresaRepository;
import com.proyecto1.thymeleaf.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock private UsuarioRepository usuarioRepository;
    @Mock private EmpresaRepository empresaRepository;
    @Mock private ModelMapper modelMapper;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private VerificacionCorreoService verificacionCorreoService;

    @InjectMocks
    private UsuarioService usuarioService;

    private UsuarioDTO datosValidos;
    private Empresa empresa;

    @BeforeEach
    void setUp() {
        datosValidos = new UsuarioDTO();
        datosValidos.setNombre("Ana Perez");
        datosValidos.setCorreo("ana@demo.com");
        datosValidos.setPassword("Clave1234");
        datosValidos.setRolAcceso(Usuario.RolAcceso.EDITOR);

        empresa = new Empresa();
        empresa.setId(1L);
    }

    @Test
    void registrarUsuario_conDatosValidos_quedaInactivoYEnviaToken() {
        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));
        when(usuarioRepository.existsByCorreoIgnoreCase("ana@demo.com")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hash");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        Usuario resultado = usuarioService.registrarUsuario(datosValidos, 1L);

        assertFalse(resultado.getActivo());
        verify(verificacionCorreoService).crearYEnviarToken(resultado);
    }

    @Test
    void registrarUsuario_conEmpresaInexistente_lanzaExcepcion() {
        when(empresaRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> usuarioService.registrarUsuario(datosValidos, 1L));
    }

    @Test
    void registrarUsuario_conCorreoDuplicado_lanzaExcepcion() {
        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));
        when(usuarioRepository.existsByCorreoIgnoreCase("ana@demo.com")).thenReturn(true);
        assertThrows(IllegalArgumentException.class, () -> usuarioService.registrarUsuario(datosValidos, 1L));
    }

    @Test
    void registrarUsuario_conPasswordDebil_lanzaExcepcion() {
        datosValidos.setPassword("debil");
        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));
        when(usuarioRepository.existsByCorreoIgnoreCase("ana@demo.com")).thenReturn(false);
        assertThrows(IllegalArgumentException.class, () -> usuarioService.registrarUsuario(datosValidos, 1L));
    }

    @Test
    void registrarUsuario_conNombreCorto_lanzaExcepcion() {
        datosValidos.setNombre("A");
        assertThrows(IllegalArgumentException.class, () -> usuarioService.registrarUsuario(datosValidos, 1L));
    }

    @Test
    void login_conCredencialesCorrectasYUsuarioActivo_devuelvePresente() {
        Usuario usuario = new Usuario();
        usuario.setCorreo("ana@demo.com");
        usuario.setPassword("hash");
        usuario.setActivo(true);

        when(usuarioRepository.findByCorreoIgnoreCase("ana@demo.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("Clave1234", "hash")).thenReturn(true);

        assertTrue(usuarioService.login("ana@demo.com", "Clave1234").isPresent());
    }

    @Test
    void login_conUsuarioInactivo_devuelveVacio() {
        Usuario usuario = new Usuario();
        usuario.setCorreo("ana@demo.com");
        usuario.setPassword("hash");
        usuario.setActivo(false);

        when(usuarioRepository.findByCorreoIgnoreCase("ana@demo.com")).thenReturn(Optional.of(usuario));

        assertTrue(usuarioService.login("ana@demo.com", "Clave1234").isEmpty());
    }

    @Test
    void login_conCorreoVacio_devuelveVacioSinConsultarRepositorio() {
        assertTrue(usuarioService.login("", "Clave1234").isEmpty());
        verifyNoInteractions(usuarioRepository);
    }

    @Test
    void login_conPasswordIncorrecta_devuelveVacio() {
        Usuario usuario = new Usuario();
        usuario.setCorreo("ana@demo.com");
        usuario.setPassword("hash");
        usuario.setActivo(true);

        when(usuarioRepository.findByCorreoIgnoreCase("ana@demo.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("Mala", "hash")).thenReturn(false);

        assertTrue(usuarioService.login("ana@demo.com", "Mala").isEmpty());
    }

    @Test
    void obtenerPorIdYEmpresa_deOtraEmpresa_lanzaExcepcion() {
        Usuario usuario = new Usuario();
        usuario.setId(5L);
        Empresa otraEmpresa = new Empresa();
        otraEmpresa.setId(99L);
        usuario.setEmpresa(otraEmpresa);

        when(usuarioRepository.findById(5L)).thenReturn(Optional.of(usuario));

        assertThrows(IllegalArgumentException.class, () -> usuarioService.obtenerPorIdYEmpresa(5L, 1L));
    }

    @Test
    void desactivarUsuario_marcaActivoFalse() {
        Usuario usuario = new Usuario();
        usuario.setId(5L);
        usuario.setEmpresa(empresa);
        usuario.setActivo(true);

        when(usuarioRepository.findById(5L)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        Usuario resultado = usuarioService.desactivarUsuario(5L, 1L);

        assertFalse(resultado.getActivo());
    }

    @Test
    void cambiarRol_conRolNulo_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> usuarioService.cambiarRol(5L, null, 1L));
    }

    @Test
    void eliminarUsuario_existente_invocaDelete() {
        Usuario usuario = new Usuario();
        usuario.setId(5L);
        usuario.setEmpresa(empresa);

        when(usuarioRepository.findById(5L)).thenReturn(Optional.of(usuario));

        usuarioService.eliminarUsuario(5L, 1L);

        verify(usuarioRepository).delete(usuario);
    }
}
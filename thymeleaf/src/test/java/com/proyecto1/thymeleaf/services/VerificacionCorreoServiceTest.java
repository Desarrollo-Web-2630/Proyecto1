package com.proyecto1.thymeleaf.services;

import com.proyecto1.thymeleaf.dto.VerificacionCorreoResponseDTO;
import com.proyecto1.thymeleaf.model.Usuario;
import com.proyecto1.thymeleaf.model.VerificacionToken;
import com.proyecto1.thymeleaf.repository.UsuarioRepository;
import com.proyecto1.thymeleaf.repository.VerificacionTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VerificacionCorreoServiceTest {

    @Mock private UsuarioRepository usuarioRepository;
    @Mock private VerificacionTokenRepository tokenRepository;
    @Mock private CorreoService correoService;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks
    private VerificacionCorreoService verificacionCorreoService;

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(verificacionCorreoService, "backendBaseUrl", "http://localhost:8080");
        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setCorreo("ana@demo.com");
        usuario.setNombre("Ana");
    }


    @Test
    void crearYEnviarToken_guardaTokenYEnviaCorreo() {
        verificacionCorreoService.crearYEnviarToken(usuario);

        verify(tokenRepository).save(any(VerificacionToken.class));
        verify(correoService).enviarCorreoVerificacion(eq("ana@demo.com"), eq("Ana"), contains("verificar-correo?token="));
    }


    @Test
    void verificarCorreo_conTokenNulo_devuelveNoVerificado() {
        VerificacionCorreoResponseDTO respuesta = verificacionCorreoService.verificarCorreo(null);
        assertFalse(respuesta.isVerificado());
    }

    @Test
    void verificarCorreo_tokenValido_activaUsuarioYMarcaUsado() {
        VerificacionToken token = new VerificacionToken("abc", usuario, Instant.now().plus(1, ChronoUnit.HOURS));
        when(tokenRepository.findByToken("abc")).thenReturn(Optional.of(token));

        VerificacionCorreoResponseDTO respuesta = verificacionCorreoService.verificarCorreo("abc");

        assertTrue(respuesta.isVerificado());
        assertTrue(usuario.getActivo());
        assertTrue(token.isUsado());
        verify(usuarioRepository).save(usuario);
    }

    @Test
    void verificarCorreo_tokenYaUsado_devuelveNoVerificado() {
        VerificacionToken token = new VerificacionToken("abc", usuario, Instant.now().plus(1, ChronoUnit.HOURS));
        token.setUsado(true);
        when(tokenRepository.findByToken("abc")).thenReturn(Optional.of(token));

        assertFalse(verificacionCorreoService.verificarCorreo("abc").isVerificado());
    }

    @Test
    void verificarCorreo_tokenExpirado_devuelveNoVerificado() {
        VerificacionToken token = new VerificacionToken("abc", usuario, Instant.now().minus(1, ChronoUnit.HOURS));
        when(tokenRepository.findByToken("abc")).thenReturn(Optional.of(token));

        assertFalse(verificacionCorreoService.verificarCorreo("abc").isVerificado());
    }

    @Test
    void verificarCorreo_noEnBDPeroSiEnDebug_devuelveVerificado() {
        when(tokenRepository.findByToken(anyString())).thenReturn(Optional.empty());
        String tokenDebug = verificacionCorreoService.generarTokenDebug("debug@demo.com");

        VerificacionCorreoResponseDTO respuesta = verificacionCorreoService.verificarCorreo(tokenDebug);

        assertTrue(respuesta.isVerificado());
        assertEquals("debug@demo.com", respuesta.getCorreo());
    }

    @Test
    void verificarCorreo_noExisteEnNingunLado_devuelveNoVerificado() {
        when(tokenRepository.findByToken("inexistente")).thenReturn(Optional.empty());
        assertFalse(verificacionCorreoService.verificarCorreo("inexistente").isVerificado());
    }


    @Test
    void activarCuenta_conTokenNulo_devuelveNoVerificado() {
        assertFalse(verificacionCorreoService.activarCuenta(null, "Clave1234").isVerificado());
    }

    @Test
    void activarCuenta_conPasswordDebil_devuelveNoVerificado() {
        assertFalse(verificacionCorreoService.activarCuenta("abc", "debil").isVerificado());
    }

    @Test
    void activarCuenta_conTokenValido_fijaPasswordYActiva() {
        VerificacionToken token = new VerificacionToken("abc", usuario, Instant.now().plus(1, ChronoUnit.HOURS));
        when(tokenRepository.findByToken("abc")).thenReturn(Optional.of(token));
        when(passwordEncoder.encode("Clave1234")).thenReturn("hashSeguro");

        VerificacionCorreoResponseDTO respuesta = verificacionCorreoService.activarCuenta("abc", "Clave1234");

        assertTrue(respuesta.isVerificado());
        assertEquals("hashSeguro", usuario.getPassword());
        assertTrue(usuario.getActivo());
    }

    @Test
    void activarCuenta_conTokenExpirado_devuelveNoVerificado() {
        VerificacionToken token = new VerificacionToken("abc", usuario, Instant.now().minus(1, ChronoUnit.HOURS));
        when(tokenRepository.findByToken("abc")).thenReturn(Optional.of(token));

        assertFalse(verificacionCorreoService.activarCuenta("abc", "Clave1234").isVerificado());
    }

    @Test
    void activarCuenta_conTokenInexistente_devuelveNoVerificado() {
        when(tokenRepository.findByToken("abc")).thenReturn(Optional.empty());
        assertFalse(verificacionCorreoService.activarCuenta("abc", "Clave1234").isVerificado());
    }


    @Test
    void reenviarVerificacion_conCorreoVacio_devuelveMensajeGenerico() {
        assertFalse(verificacionCorreoService.reenviarVerificacion(" ").isVerificado());
    }

    @Test
    void reenviarVerificacion_conCorreoInexistente_devuelveMensajeGenerico() {
        when(usuarioRepository.findByCorreoIgnoreCase("noexiste@demo.com")).thenReturn(Optional.empty());

        VerificacionCorreoResponseDTO respuesta = verificacionCorreoService.reenviarVerificacion("noexiste@demo.com");

        assertFalse(respuesta.isVerificado());
        verifyNoInteractions(tokenRepository);
    }

    @Test
    void reenviarVerificacion_conUsuarioYaActivo_noEnviaNada() {
        usuario.setActivo(true);
        when(usuarioRepository.findByCorreoIgnoreCase("ana@demo.com")).thenReturn(Optional.of(usuario));
        when(tokenRepository.countByUsuarioIdAndExpiracionAfter(eq(1L), any(Instant.class))).thenReturn(0L);

        verificacionCorreoService.reenviarVerificacion("ana@demo.com");

        verify(correoService, never()).enviarCorreoVerificacion(any(), any(), any());
    }

    @Test
    void reenviarVerificacion_conLimiteAlcanzado_noEnviaNada() {
        usuario.setActivo(false);
        when(usuarioRepository.findByCorreoIgnoreCase("ana@demo.com")).thenReturn(Optional.of(usuario));
        when(tokenRepository.countByUsuarioIdAndExpiracionAfter(eq(1L), any(Instant.class)))
                .thenReturn((long) VerificacionCorreoService.MAX_REENVIOS_POR_HORA);

        verificacionCorreoService.reenviarVerificacion("ana@demo.com");

        verify(correoService, never()).enviarCorreoVerificacion(any(), any(), any());
    }

    @Test
    void reenviarVerificacion_conUsuarioInactivoYSinLimite_enviaNuevoToken() {
        usuario.setActivo(false);
        when(usuarioRepository.findByCorreoIgnoreCase("ana@demo.com")).thenReturn(Optional.of(usuario));
        when(tokenRepository.countByUsuarioIdAndExpiracionAfter(eq(1L), any(Instant.class))).thenReturn(0L);

        verificacionCorreoService.reenviarVerificacion("ana@demo.com");

        verify(correoService).enviarCorreoVerificacion(eq("ana@demo.com"), eq("Ana"), anyString());
    }


    @Test
    void generarTokenDebug_devuelveUnTokenNoVacio() {
        String token = verificacionCorreoService.generarTokenDebug("debug@demo.com");
        assertNotNull(token);
        assertFalse(token.isBlank());
    }

    @Test
    void getBackendBaseUrl_devuelveElValorConfigurado() {
        assertEquals("http://localhost:8080", verificacionCorreoService.getBackendBaseUrl());
    }
}
package com.proyecto1.thymeleaf.services;

import com.proyecto1.thymeleaf.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * CorreoService tiene 4 caminos relevantes según la configuración:
 * 1. Correo desactivado (app.mail.enabled=false): solo se loguea, nunca se llama al MailSender.
 * 2. Correo activado pero sin SMTP configurado: falla silencioso o lanza, según app.mail.fail-on-error.
 * 3. Correo activado con SMTP: envío exitoso.
 * 4. Correo activado con SMTP: el envío falla (MailException) y se respeta fail-on-error.
 */
@ExtendWith(MockitoExtension.class)
class CorreoServiceTest {

    @Mock private UsuarioRepository usuarioRepository;
    @Mock private ObjectProvider<JavaMailSender> mailSenderProvider;
    @Mock private JavaMailSender mailSender;

    @InjectMocks
    private CorreoService correoService;

    private static final String DESTINATARIO = "usuario@demo.com";
    private static final String NOMBRE = "Ana";
    private static final String ENLACE = "http://localhost:8080/api/auth/verificar-correo?token=abc123";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(correoService, "remitente", "no-reply@empresa.local");
    }

    @Test
    void correoDesactivado_noConsultaElMailSenderYNoLanza() {
        ReflectionTestUtils.setField(correoService, "mailEnabled", false);

        assertDoesNotThrow(() -> correoService.enviarCorreoVerificacion(DESTINATARIO, NOMBRE, ENLACE));

        verifyNoInteractions(mailSenderProvider);
    }

    @Test
    void correoActivadoSinSmtp_yFailOnErrorFalso_noLanza() {
        ReflectionTestUtils.setField(correoService, "mailEnabled", true);
        ReflectionTestUtils.setField(correoService, "failOnError", false);
        when(mailSenderProvider.getIfAvailable()).thenReturn(null);

        assertDoesNotThrow(() -> correoService.enviarCorreoVerificacion(DESTINATARIO, NOMBRE, ENLACE));
    }

    @Test
    void correoActivadoSinSmtp_yFailOnErrorTrue_lanzaExcepcion() {
        ReflectionTestUtils.setField(correoService, "mailEnabled", true);
        ReflectionTestUtils.setField(correoService, "failOnError", true);
        when(mailSenderProvider.getIfAvailable()).thenReturn(null);

        assertThrows(IllegalStateException.class,
                () -> correoService.enviarCorreoVerificacion(DESTINATARIO, NOMBRE, ENLACE));
    }

    @Test
    void correoActivadoConSmtpDisponible_enviaElMensajeConDatosCorrectos() {
        ReflectionTestUtils.setField(correoService, "mailEnabled", true);
        when(mailSenderProvider.getIfAvailable()).thenReturn(mailSender);

        correoService.enviarCorreoVerificacion(DESTINATARIO, NOMBRE, ENLACE);

        ArgumentCaptorHolder<SimpleMailMessage> captor = new ArgumentCaptorHolder<>(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());

        SimpleMailMessage enviado = captor.getValue();
        assertEquals(DESTINATARIO, enviado.getTo()[0]);
        assertEquals("no-reply@empresa.local", enviado.getFrom());
        assertTrue(enviado.getText().contains(ENLACE));
        assertTrue(enviado.getText().contains(NOMBRE));
    }

    @Test
    void correoActivadoConSmtp_nombreNuloUsaSaludoGenerico() {
        ReflectionTestUtils.setField(correoService, "mailEnabled", true);
        when(mailSenderProvider.getIfAvailable()).thenReturn(mailSender);

        correoService.enviarCorreoVerificacion(DESTINATARIO, null, ENLACE);

        ArgumentCaptorHolder<SimpleMailMessage> captor = new ArgumentCaptorHolder<>(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());
        assertTrue(captor.getValue().getText().startsWith("Hola,"));
    }

    @Test
    void envioFalla_yFailOnErrorFalso_noPropagaLaExcepcion() {
        ReflectionTestUtils.setField(correoService, "mailEnabled", true);
        ReflectionTestUtils.setField(correoService, "failOnError", false);
        when(mailSenderProvider.getIfAvailable()).thenReturn(mailSender);
        doThrow(new MailSendException("SMTP caído")).when(mailSender).send(any(SimpleMailMessage.class));

        assertDoesNotThrow(() -> correoService.enviarCorreoVerificacion(DESTINATARIO, NOMBRE, ENLACE));
    }

    @Test
    void envioFalla_yFailOnErrorTrue_lanzaIllegalStateException() {
        ReflectionTestUtils.setField(correoService, "mailEnabled", true);
        ReflectionTestUtils.setField(correoService, "failOnError", true);
        when(mailSenderProvider.getIfAvailable()).thenReturn(mailSender);
        doThrow(new MailSendException("SMTP caído")).when(mailSender).send(any(SimpleMailMessage.class));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> correoService.enviarCorreoVerificacion(DESTINATARIO, NOMBRE, ENLACE));
        assertTrue(ex.getMessage().contains("verificación"));
    }

    @Test
    void correoExiste_conCorreoRegistrado_devuelveTrue() {
        when(usuarioRepository.existsByCorreoIgnoreCase(DESTINATARIO)).thenReturn(true);
        assertTrue(correoService.correoExiste(DESTINATARIO));
    }

    @Test
    void correoExiste_conCorreoNoRegistrado_devuelveFalse() {
        when(usuarioRepository.existsByCorreoIgnoreCase(DESTINATARIO)).thenReturn(false);
        assertFalse(correoService.correoExiste(DESTINATARIO));
    }

    @Test
    void correoExiste_conValorNuloOBlanco_devuelveFalseSinConsultarRepositorio() {
        assertFalse(correoService.correoExiste(null));
        assertFalse(correoService.correoExiste("   "));
        verifyNoInteractions(usuarioRepository);
    }

    /**
     * Mockito's ArgumentCaptor no se puede instanciar directo con "new" en todas las
     * versiones sin @Captor; este pequeño wrapper evita el warning de generics.
     */
    private static class ArgumentCaptorHolder<T> {
        private final org.mockito.ArgumentCaptor<T> captor;

        ArgumentCaptorHolder(Class<T> type) {
            this.captor = org.mockito.ArgumentCaptor.forClass(type);
        }

        T capture() {
            return captor.capture();
        }

        T getValue() {
            return captor.getValue();
        }
    }
}
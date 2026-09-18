package com.proyecto1.thymeleaf.services;

import com.proyecto1.thymeleaf.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Prueba unitaria pura (sin contexto de Spring) del envio del correo de
 * verificacion, con JavaMailSender simulado: se comprueba que se envia lo
 * que se debe, y como se comporta cuando el SMTP falla.
 */
class CorreoServiceTest {

    private JavaMailSender mailSender;
    private CorreoService correoService;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void preparar() {
        mailSender = mock(JavaMailSender.class);
        ObjectProvider<JavaMailSender> proveedor = mock(ObjectProvider.class);
        when(proveedor.getIfAvailable()).thenReturn(mailSender);

        correoService = new CorreoService(mock(UsuarioRepository.class), proveedor);
        ReflectionTestUtils.setField(correoService, "mailEnabled", true);
        ReflectionTestUtils.setField(correoService, "failOnError", false);
        ReflectionTestUtils.setField(correoService, "remitente", "no-reply@prueba.com");
    }

    @Test
    void enviaElCorreoConDestinatarioAsuntoYEnlace() {
        correoService.enviarCorreoVerificacion("ana@prueba.com", "Ana", "http://localhost:8080/api/auth/verificar-correo?token=abc123");

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());
        SimpleMailMessage enviado = captor.getValue();

        assertEquals("no-reply@prueba.com", enviado.getFrom());
        assertEquals("ana@prueba.com", enviado.getTo()[0]);
        assertEquals("Confirma tu correo", enviado.getSubject());
        assertTrue(enviado.getText().contains("Hola Ana"));
        assertTrue(enviado.getText().contains("token=abc123"));
    }

    @Test
    void conElCorreoDesactivadoNoTocaElSmtp() {
        ReflectionTestUtils.setField(correoService, "mailEnabled", false);

        correoService.enviarCorreoVerificacion("ana@prueba.com", "Ana", "http://enlace");

        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void siElSmtpFallaYNoEsCriticoNoRevientaElRegistro() {
        doThrow(new MailSendException("SMTP caido")).when(mailSender).send(any(SimpleMailMessage.class));

        // fail-on-error=false: el registro de la empresa no debe fallar porque
        // el correo no salio; el enlace queda en el log y se puede reenviar.
        assertDoesNotThrow(() -> correoService.enviarCorreoVerificacion("ana@prueba.com", "Ana", "http://enlace"));
    }

    @Test
    void siElSmtpFallaYEsCriticoLanzaUnaExcepcionClara() {
        ReflectionTestUtils.setField(correoService, "failOnError", true);
        doThrow(new MailSendException("SMTP caido")).when(mailSender).send(any(SimpleMailMessage.class));

        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> correoService.enviarCorreoVerificacion("ana@prueba.com", "Ana", "http://enlace"));

        assertEquals("No se pudo enviar el correo de verificación", error.getMessage());
    }

    @Test
    @SuppressWarnings("unchecked")
    void sinConfiguracionSmtpYModoCriticoLanzaExcepcion() {
        ObjectProvider<JavaMailSender> sinSender = mock(ObjectProvider.class);
        when(sinSender.getIfAvailable()).thenReturn(null);
        CorreoService sinSmtp = new CorreoService(mock(UsuarioRepository.class), sinSender);
        ReflectionTestUtils.setField(sinSmtp, "mailEnabled", true);
        ReflectionTestUtils.setField(sinSmtp, "failOnError", true);
        ReflectionTestUtils.setField(sinSmtp, "remitente", "no-reply@prueba.com");

        assertThrows(IllegalStateException.class,
                () -> sinSmtp.enviarCorreoVerificacion("ana@prueba.com", "Ana", "http://enlace"));
    }
}

package com.proyecto1.thymeleaf.services;

import com.proyecto1.thymeleaf.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class CorreoService {

    private static final Logger log = LoggerFactory.getLogger(CorreoService.class);

    private final UsuarioRepository usuarioRepository;
    private final ObjectProvider<JavaMailSender> mailSenderProvider;

    @Value("${app.mail.enabled:false}")
    private boolean mailEnabled;

    @Value("${app.mail.fail-on-error:false}")
    private boolean failOnError;

    @Value("${app.mail.from:no-reply@empresa.local}")
    private String remitente;

    public CorreoService(UsuarioRepository usuarioRepository,
                         ObjectProvider<JavaMailSender> mailSenderProvider) {
        this.usuarioRepository = usuarioRepository;
        this.mailSenderProvider = mailSenderProvider;
    }

    public boolean correoExiste(String correo) {
        if (correo == null || correo.isBlank()) {
            return false;
        }
        return usuarioRepository.existsByCorreoIgnoreCase(correo.trim());
    }

    public void enviarCorreoVerificacion(String destinatario, String nombre, String enlaceVerificacion) {
        log.info("Intentando enviar correo de verificación para {}", destinatario);

        if (!mailEnabled) {
            log.info("Correo desactivado. Enlace de verificación para {}: {}", destinatario, enlaceVerificacion);
            return;
        }

        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null) {
            String mensaje = "No hay configuración SMTP disponible para enviar correos";
            log.warn("{} | destinatario={}", mensaje, destinatario);
            if (failOnError) {
                throw new IllegalStateException(mensaje);
            }
            return;
        }

        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setFrom(remitente);
        mensaje.setTo(destinatario);
        mensaje.setSubject("Confirma tu correo");
        mensaje.setText(construirCuerpo(nombre, enlaceVerificacion));

        try {
            mailSender.send(mensaje);
            log.info("Correo de verificación enviado a {}", destinatario);
        } catch (MailException e) {
            log.error("No se pudo enviar el correo a {}. Enlace: {}", destinatario, enlaceVerificacion, e);
            if (failOnError) {
                throw new IllegalStateException("No se pudo enviar el correo de verificación", e);
            }
        }
    }

    private String construirCuerpo(String nombre, String enlaceVerificacion) {
        String saludo = (nombre == null || nombre.isBlank()) ? "Hola" : "Hola " + nombre.trim();

        return saludo + ",\n\n"
                + "Gracias por registrarte. Para confirmar tu cuenta, usa el siguiente enlace:\n\n"
                + enlaceVerificacion + "\n\n"
                + "Si no pediste esta cuenta, puedes ignorar este mensaje.\n\n"
                + "Equipo del sistema";
    }
}
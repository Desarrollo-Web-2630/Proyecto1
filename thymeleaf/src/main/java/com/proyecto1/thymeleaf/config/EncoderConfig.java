package com.proyecto1.thymeleaf.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Solo el encoder de contrasenas. Las reglas de autenticacion y autorizacion
 * viven en SecurityConfig; antes estaban mezcladas aqui con un
 * anyRequest().permitAll() que dejaba toda la API abierta.
 */
@Configuration
public class EncoderConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

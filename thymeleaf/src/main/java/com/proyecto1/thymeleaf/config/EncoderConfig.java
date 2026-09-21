package com.proyecto1.thymeleaf.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Solo el encoder de contrasenas (BCrypt), que viene de spring-security-crypto.
 * En esta entrega no hay autenticacion ni autorizacion: Spring Security
 * completo (filtros, login, tokens) es de la entrega final.
 */
@Configuration
public class EncoderConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

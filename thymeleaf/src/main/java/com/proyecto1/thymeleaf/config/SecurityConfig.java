package com.proyecto1.thymeleaf.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Entrega 2: sin autenticacion. Toda la API es publica; la empresa de cada
 * peticion se toma del encabezado X-Empresa-Id (ver util.EmpresaActual).
 *
 * Spring Security sigue en el classpath porque BCrypt (PasswordEncoder)
 * viene de ahi; sin esta clase, el starter protegeria todo con HTTP Basic y
 * una contrasena generada al azar.
 *
 * La autenticacion real (JWT, roles, aislamiento por token) es de la
 * entrega 3 y ya esta hecha en la rama respaldo-seguridad-entrega3.
 */
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(sesion -> sesion.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }
}

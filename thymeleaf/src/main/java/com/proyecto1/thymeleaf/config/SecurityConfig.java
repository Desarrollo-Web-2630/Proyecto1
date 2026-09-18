package com.proyecto1.thymeleaf.config;

import com.proyecto1.thymeleaf.security.JwtAuthenticationFilter;
import com.proyecto1.thymeleaf.security.JwtService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Reglas de autenticacion HTTP. No hay modo "sin seguridad": toda ruta
 * fuera de las publicas exige un JWT valido, en todos los perfiles. Para
 * probar con Postman, la coleccion captura el token del login sola (ver
 * README); no hace falta desactivar nada.
 *
 * Publicas, porque son las que necesariamente ocurren antes de tener un
 * token: registrar una empresa, iniciar sesion, verificar el correo,
 * activar la cuenta y reenviar la verificacion.
 *
 * El filtro JWT lee "Authorization: Bearer", valida y deja un
 * UsuarioPrincipal en el contexto; los controladores sacan de ahi la
 * empresa y el rol (ContextoSeguridad), nunca de un id fijo ni de la URL.
 *
 * Las reglas de "solo administrador" o "solo lectura no puede escribir" no
 * viven aqui: se verifican dentro de cada servicio via ContextoSeguridad,
 * para que la autorizacion no dependa unicamente de esta configuracion.
 */
@Configuration
public class SecurityConfig {

    private static final String[] RUTAS_PUBLICAS = {
            "/api/v1/usuarios/login",     // POST: inicio de sesion (HU-03)
            "/api/auth/**"                // verificar-correo, activar-cuenta, reenviar-verificacion
    };

    private final JwtService jwtService;

    public SecurityConfig(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(sesion -> sesion.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .addFilterBefore(new JwtAuthenticationFilter(jwtService), UsernamePasswordAuthenticationFilter.class)
            .authorizeHttpRequests(auth -> auth
                    // POST /api/v1/empresas es publico (registro de empresa, HU-01);
                    // el resto de ese path (GET, PUT, DELETE) si exige autenticacion.
                    .requestMatchers(HttpMethod.POST, "/api/v1/empresas").permitAll()
                    .requestMatchers(RUTAS_PUBLICAS).permitAll()
                    .anyRequest().authenticated())
            // Sin esto, Spring Security responde 403 tanto a quien no manda
            // ningun token como a quien manda uno valido pero sin permiso: el
            // valor por defecto (Http403ForbiddenEntryPoint) no distingue "no
            // autenticado" de "autenticado pero sin permiso". Con esto, "sin
            // token" o "token invalido" es 401, y "autenticado pero sin el rol
            // necesario" sigue siendo 403 (via AccessDeniedException, que
            // GlobalExceptionHandler ya traduce).
            .exceptionHandling(manejo -> manejo
                    .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)));

        return http.build();
    }
}

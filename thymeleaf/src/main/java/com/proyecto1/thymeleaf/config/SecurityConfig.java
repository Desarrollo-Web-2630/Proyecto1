package com.proyecto1.thymeleaf.config;

import com.proyecto1.thymeleaf.security.JwtAuthenticationFilter;
import com.proyecto1.thymeleaf.security.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;

/**
 * Reglas de autenticacion HTTP.
 *
 * El filtro JWT SIEMPRE corre, sin importar app.security.enabled: si llega un
 * "Authorization: Bearer" valido, deja el UsuarioPrincipal en el contexto.
 * Los controladores leen la empresa y el rol de ahi (ContextoSeguridad), no
 * de un id fijo; si el filtro no corriera con la bandera en false, ningun
 * endpoint de negocio podria saber de que empresa es la peticion aunque el
 * cliente si mandara un token valido.
 *
 * Lo que si cambia con la bandera es authorizeHttpRequests():
 * - true (el valor por defecto: ver application.properties): toda ruta fuera
 *   de las publicas exige que el filtro haya autenticado la peticion. Sin
 *   token valido, Spring Security corta con 401 antes de llegar al
 *   controlador.
 * - false (perfiles h2, local, postman, para probar con Postman sin generar
 *   un token en cada corrida): permitAll() a nivel de ruta. Igual, cualquier
 *   endpoint que internamente llame a ContextoSeguridad.empresaIdActual()
 *   sigue exigiendo un token real, porque sin el no hay ninguna empresa que
 *   consultar; el GlobalExceptionHandler convierte esa falta en un 401 claro
 *   en vez de un 500.
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

    @Value("${app.security.enabled:true}")
    private boolean seguridadHabilitada;

    private final JwtService jwtService;

    public SecurityConfig(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(sesion -> sesion.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .addFilterBefore(new JwtAuthenticationFilter(jwtService), UsernamePasswordAuthenticationFilter.class);

        if (!seguridadHabilitada) {
            http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
            return http.build();
        }

        http.authorizeHttpRequests(auth -> auth
                    // POST /api/v1/empresas es publico (registro de empresa, HU-01);
                    // el resto de ese path (GET, PUT, DELETE) si exige autenticacion.
                    .requestMatchers(HttpMethod.POST, "/api/v1/empresas").permitAll()
                    .requestMatchers(RUTAS_PUBLICAS).permitAll()
                    .anyRequest().authenticated())
            // Sin esto, Spring Security responde 403 tanto quien no manda
            // ningun token como quien manda uno valido pero sin permiso: el
            // valor por defecto (Http403ForbiddenEntryPoint) no distingue
            // "no autenticado" de "autenticado pero sin permiso". Con esto,
            // "sin token" o "token invalido" es 401, y "autenticado pero sin
            // el rol necesario" sigue siendo 403 (via AccessDeniedException,
            // que GlobalExceptionHandler ya traduce).
            .exceptionHandling(manejo -> manejo
                    .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)));

        return http.build();
    }
}

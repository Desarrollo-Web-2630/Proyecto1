package com.proyecto1.thymeleaf.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final String ADMIN = "ADMIN";
    private static final String EDITOR = "EDITOR";
    private static final String API = "/api/v1/**";

    @Bean
    public SecurityFilterChain filtroSeguridad(HttpSecurity http, JwtServicio jwtServicio,
                                               UserDetailsService userDetailsService,
                                               ObjectMapper objectMapper) throws Exception {
        ManejadorErroresSeguridad errores = new ManejadorErroresSeguridad(objectMapper);

        http
                // API sin estado con token en el encabezado: no hay cookie de sesion que proteger con CSRF
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .exceptionHandling(e -> e.authenticationEntryPoint(errores).accessDeniedHandler(errores))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // Publicas: login, activacion de cuenta, registro de empresa y documentacion
                        .requestMatchers("/api/auth/yo").authenticated()
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/empresas").permitAll()
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**", "/error").permitAll()
                        .requestMatchers("/api/v1/debug/**").hasRole(ADMIN)
                        // Consultar: cualquier rol de la empresa
                        .requestMatchers(HttpMethod.GET, API).authenticated()
                        // Eliminar, administrar usuarios, empresa y roles: solo ADMIN
                        .requestMatchers(HttpMethod.DELETE, API).hasRole(ADMIN)
                        .requestMatchers("/api/v1/usuarios/**", "/api/v1/empresas/**", "/api/v1/roles-proceso/**").hasRole(ADMIN)
                        .requestMatchers(HttpMethod.POST, "/api/v1/procesos/*/inactivar", "/api/v1/procesos/*/reactivar").hasRole(ADMIN)
                        // Crear y editar procesos y su diagrama: ADMIN y EDITOR
                        .requestMatchers(API).hasAnyRole(ADMIN, EDITOR)
                        .anyRequest().authenticated())
                .addFilterBefore(new JwtFiltro(jwtServicio, userDetailsService), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(UserDetailsService userDetailsService,
                                                       PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider proveedor = new DaoAuthenticationProvider();
        proveedor.setUserDetailsService(userDetailsService);
        proveedor.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(proveedor);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(
            @Value("${app.cors.origenes:http://localhost:4200}") String[] origenes) {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(origenes));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        config.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource fuente = new UrlBasedCorsConfigurationSource();
        fuente.registerCorsConfiguration("/api/**", config);
        return fuente;
    }
}

package com.proyecto1.thymeleaf.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    public static final String ESQUEMA_SEGURIDAD = "bearerAuth";

    // Swagger UI muestra el boton "Authorize": se pega el token del login y se envia como Bearer
    @Bean
    public OpenAPI apiInfo() {
        return new OpenAPI()
                .info(new Info()
                        .title("Editor de procesos BPMN - API")
                        .version("v1")
                        .description("Backend Spring Boot + JPA con Spring Security. Autenticacion con JWT: "
                                + "POST /api/v1/usuarios/login devuelve el token, que se envia como "
                                + "'Authorization: Bearer <token>'. La empresa se toma del token."))
                .components(new Components().addSecuritySchemes(ESQUEMA_SEGURIDAD, new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList(ESQUEMA_SEGURIDAD));
    }
}

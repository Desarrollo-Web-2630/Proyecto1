package com.proyecto1.thymeleaf.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OpenApiConfigTest {

    private final OpenApiConfig config = new OpenApiConfig();

    @Test
    void apiInfo_defineTituloVersionYDescripcion() {
        OpenAPI openApi = config.apiInfo();

        assertNotNull(openApi.getInfo());
        assertEquals("Editor de procesos BPMN - API", openApi.getInfo().getTitle());
        assertEquals("v1", openApi.getInfo().getVersion());
        assertTrue(openApi.getInfo().getDescription().contains("JWT"));
    }

    @Test
    void apiInfo_declaraElEsquemaBearerJwt() {
        OpenAPI openApi = config.apiInfo();

        SecurityScheme esquema = openApi.getComponents().getSecuritySchemes().get(OpenApiConfig.ESQUEMA);
        assertEquals(SecurityScheme.Type.HTTP, esquema.getType());
        assertEquals("bearer", esquema.getScheme());
        assertEquals("JWT", esquema.getBearerFormat());
        assertTrue(openApi.getSecurity().get(0).containsKey(OpenApiConfig.ESQUEMA));
    }
}

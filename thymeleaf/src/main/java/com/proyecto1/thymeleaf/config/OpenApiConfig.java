package com.proyecto1.thymeleaf.config;

import com.proyecto1.thymeleaf.util.EmpresaActual;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.HeaderParameter;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI apiInfo() {
        return new OpenAPI().info(new Info()
                .title("Editor de procesos BPMN - API")
                .version("v1")
                .description("Backend Spring Boot + JPA. Empresas, usuarios y procesos con "
                        + "aislamiento por empresa. Sin autenticacion en esta entrega: la empresa "
                        + "se indica con el encabezado X-Empresa-Id (por defecto 1)."));
    }

    @Bean
    public OpenApiCustomizer encabezadoEmpresa() {
        return openApi -> openApi.getPaths().forEach((ruta, item) -> {
            if (ruta.startsWith("/api/v1/")) {
                item.readOperations().forEach(op -> op.addParametersItem(new HeaderParameter()
                        .name(EmpresaActual.ENCABEZADO)
                        .description("Id de la empresa a la que pertenece la peticion")
                        .required(false)
                        .schema(new StringSchema()._default("1"))));
            }
        });
    }
}

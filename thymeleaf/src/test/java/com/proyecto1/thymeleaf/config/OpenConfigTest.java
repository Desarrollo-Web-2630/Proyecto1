package com.proyecto1.thymeleaf.config;

import com.proyecto1.thymeleaf.util.EmpresaActual;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.junit.jupiter.api.Test;
import org.springdoc.core.customizers.OpenApiCustomizer;

import static org.junit.jupiter.api.Assertions.*;

class OpenApiConfigTest {

    private final OpenApiConfig config = new OpenApiConfig();

    @Test
    void apiInfo_defineTituloVersionYDescripcion() {
        OpenAPI openApi = config.apiInfo();

        assertNotNull(openApi.getInfo());
        assertEquals("Editor de procesos BPMN - API", openApi.getInfo().getTitle());
        assertEquals("v1", openApi.getInfo().getVersion());
        assertTrue(openApi.getInfo().getDescription().contains("X-Empresa-Id"));
    }

    @Test
    void encabezadoEmpresa_agregaElHeaderSoloEnRutasDeNegocio() {
        OpenApiCustomizer customizer = config.encabezadoEmpresa();

        Operation operacionNegocio = new Operation();
        PathItem itemNegocio = new PathItem().get(operacionNegocio);

        Operation operacionOtra = new Operation();
        PathItem itemOtro = new PathItem().get(operacionOtra);

        Paths paths = new Paths();
        paths.addPathItem("/api/v1/empresas", itemNegocio);
        paths.addPathItem("/actuator/health", itemOtro);

        OpenAPI openApi = new OpenAPI().paths(paths);

        customizer.customise(openApi);

        boolean tieneHeaderEnNegocio = operacionNegocio.getParameters() != null
                && operacionNegocio.getParameters().stream()
                        .anyMatch(p -> EmpresaActual.ENCABEZADO.equals(p.getName()));
        assertTrue(tieneHeaderEnNegocio);

        boolean tieneHeaderEnOtra = operacionOtra.getParameters() != null
                && operacionOtra.getParameters().stream()
                        .anyMatch(p -> EmpresaActual.ENCABEZADO.equals(p.getName()));
        assertFalse(tieneHeaderEnOtra);
    }

    @Test
    void encabezadoEmpresa_elParametroAgregadoEsOpcionalConDefault1() {
        OpenApiCustomizer customizer = config.encabezadoEmpresa();

        Operation operacion = new Operation();
        PathItem item = new PathItem().get(operacion);

        Paths paths = new Paths();
        paths.addPathItem("/api/v1/procesos", item);

        OpenAPI openApi = new OpenAPI().paths(paths);
        customizer.customise(openApi);

        Parameter parametro = operacion.getParameters().get(0);
        assertEquals("header", parametro.getIn());
        assertFalse(parametro.getRequired());
        assertEquals("1", parametro.getSchema().getDefault());
    }
}
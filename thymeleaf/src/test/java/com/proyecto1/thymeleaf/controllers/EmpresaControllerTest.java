package com.proyecto1.thymeleaf.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyecto1.thymeleaf.dto.EmpresaDTO;
import com.proyecto1.thymeleaf.model.Empresa;
import com.proyecto1.thymeleaf.services.EmpresaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EmpresaController.class)
class EmpresaControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private EmpresaService empresaService;

    @Test
    void listar_devuelveOkConLaListaDelService() throws Exception {
        Empresa empresa = new Empresa();
        empresa.setId(1L);
        empresa.setNombre("Empresa Demo");
        empresa.setNit("900123456-7");
        empresa.setCorreo("contacto@demo.com");

        when(empresaService.listarTodas()).thenReturn(List.of(empresa));

        mockMvc.perform(get("/api/v1/empresas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Empresa Demo"))
                .andExpect(jsonPath("$[0].nit").value("900123456-7"));
    }

    @Test
    void obtener_existente_devuelveOk() throws Exception {
        Empresa empresa = new Empresa();
        empresa.setId(1L);
        empresa.setNombre("Empresa Demo");
        when(empresaService.obtenerPorId(1L)).thenReturn(empresa);

        mockMvc.perform(get("/api/v1/empresas/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Empresa Demo"));
    }

    @Test
    void obtener_inexistente_devuelve400ViaGlobalExceptionHandler() throws Exception {
        when(empresaService.obtenerPorId(99L))
                .thenThrow(new IllegalArgumentException("La empresa no existe"));

        mockMvc.perform(get("/api/v1/empresas/99"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("La empresa no existe"));
    }

    @Test
    void crear_conDatosValidos_devuelve201() throws Exception {
        EmpresaDTO datos = new EmpresaDTO();
        datos.setNombre("Empresa Nueva");
        datos.setNit("900999999-1");
        datos.setCorreo("nueva@demo.com");

        Empresa creada = new Empresa();
        creada.setId(5L);
        creada.setNombre("Empresa Nueva");
        creada.setNit("900999999-1");
        creada.setCorreo("nueva@demo.com");

        when(empresaService.registrarEmpresa(any(EmpresaDTO.class))).thenReturn(creada);

        mockMvc.perform(post("/api/v1/empresas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(datos)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.nombre").value("Empresa Nueva"));
    }

    @Test
    void crear_conNombreVacio_devuelve400PorValidacionDeBean() throws Exception {
        EmpresaDTO datos = new EmpresaDTO();
        datos.setNombre("");
        datos.setNit("900999999-1");
        datos.setCorreo("nueva@demo.com");

        mockMvc.perform(post("/api/v1/empresas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(datos)))
                .andExpect(status().isBadRequest());

        verify(empresaService, never()).registrarEmpresa(any());
    }

    @Test
    void crear_conNitDuplicado_devuelve400ConElMensajeDelService() throws Exception {
        EmpresaDTO datos = new EmpresaDTO();
        datos.setNombre("Empresa Nueva");
        datos.setNit("900999999-1");
        datos.setCorreo("nueva@demo.com");

        when(empresaService.registrarEmpresa(any(EmpresaDTO.class)))
                .thenThrow(new IllegalArgumentException("Ya existe una empresa registrada con el NIT '900999999-1'"));

        mockMvc.perform(post("/api/v1/empresas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(datos)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value(org.hamcrest.Matchers.containsString("NIT")));
    }

    @Test
    void actualizar_conDatosValidos_devuelveOk() throws Exception {
        EmpresaDTO datos = new EmpresaDTO();
        datos.setNombre("Empresa Actualizada");
        datos.setNit("900123456-7");
        datos.setCorreo("contacto@demo.com");

        Empresa actualizada = new Empresa();
        actualizada.setId(1L);
        actualizada.setNombre("Empresa Actualizada");
        actualizada.setNit("900123456-7");
        actualizada.setCorreo("contacto@demo.com");

        when(empresaService.actualizarEmpresa(eq(1L), any(EmpresaDTO.class))).thenReturn(actualizada);

        mockMvc.perform(put("/api/v1/empresas/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(datos)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Empresa Actualizada"));
    }

    @Test
    void eliminar_existente_devuelve204() throws Exception {
        mockMvc.perform(delete("/api/v1/empresas/1"))
                .andExpect(status().isNoContent());

        verify(empresaService).eliminarEmpresa(1L);
    }
}
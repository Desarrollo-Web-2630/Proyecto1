package com.proyecto1.thymeleaf.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyecto1.thymeleaf.dto.RolProcesoRequestDTO;
import com.proyecto1.thymeleaf.dto.RolProcesoRespuestaDTO;
import com.proyecto1.thymeleaf.services.RolProcesoService;

@WebMvcTest(RolProcesoController.class)
class RolProcesoControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private RolProcesoService rolProcesoService;

    private RolProcesoRespuestaDTO analista() {
        return new RolProcesoRespuestaDTO(1L, "Analista", "Revisa solicitudes", List.of(), true);
    }

    private RolProcesoRequestDTO datos(String nombre, String descripcion) {
        RolProcesoRequestDTO dto = new RolProcesoRequestDTO();
        dto.setNombre(nombre);
        dto.setDescripcion(descripcion);
        return dto;
    }

    @Test
    void listar_devuelveOkConLosRolesDeLaEmpresa() throws Exception {
        when(rolProcesoService.listarPorEmpresa(anyLong())).thenReturn(List.of(analista()));

        mockMvc.perform(get("/api/v1/roles-proceso"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Analista"))
                .andExpect(jsonPath("$[0].eliminable").value(true));
    }

    @Test
    void buscar_conNombre_devuelvePaginaDeResultados() throws Exception {
        when(rolProcesoService.buscar(anyLong(), eq("anal"), any()))
                .thenReturn(new PageImpl<>(List.of(analista())));

        mockMvc.perform(get("/api/v1/roles-proceso/buscar").param("nombre", "anal"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].nombre").value("Analista"));
    }

    @Test
    void buscar_sinNombre_pasaNuloAlService() throws Exception {
        when(rolProcesoService.buscar(anyLong(), isNull(), any()))
                .thenReturn(new PageImpl<>(List.of(analista())));

        mockMvc.perform(get("/api/v1/roles-proceso/buscar").param("page", "0").param("size", "500"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1));
    }

    @Test
    void obtener_devuelveElRol() throws Exception {
        when(rolProcesoService.obtenerPorIdYEmpresa(eq(1L), anyLong())).thenReturn(analista());

        mockMvc.perform(get("/api/v1/roles-proceso/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.descripcion").value("Revisa solicitudes"));
    }

    @Test
    void obtener_inexistente_devuelve400() throws Exception {
        when(rolProcesoService.obtenerPorIdYEmpresa(eq(99L), anyLong()))
                .thenThrow(new IllegalArgumentException("El rol no existe o no pertenece a su empresa"));

        mockMvc.perform(get("/api/v1/roles-proceso/99"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("El rol no existe o no pertenece a su empresa"));
    }

    @Test
    void crear_conDatosValidos_devuelve201() throws Exception {
        when(rolProcesoService.crearRol(any(RolProcesoRequestDTO.class), anyLong())).thenReturn(analista());

        mockMvc.perform(post("/api/v1/roles-proceso")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(datos("Analista", "Revisa solicitudes"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void crear_sinNombre_devuelve400() throws Exception {
        mockMvc.perform(post("/api/v1/roles-proceso")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(datos("", "Revisa solicitudes"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void crear_sinDescripcion_devuelve400() throws Exception {
        mockMvc.perform(post("/api/v1/roles-proceso")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(datos("Analista", " "))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void crear_conNombreDuplicado_devuelve400() throws Exception {
        when(rolProcesoService.crearRol(any(RolProcesoRequestDTO.class), anyLong()))
                .thenThrow(new IllegalArgumentException("Ya existe un rol llamado 'Analista' en la empresa"));

        mockMvc.perform(post("/api/v1/roles-proceso")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(datos("Analista", "Revisa solicitudes"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void actualizar_conDatosValidos_devuelve200() throws Exception {
        when(rolProcesoService.actualizarRol(eq(1L), any(RolProcesoRequestDTO.class), anyLong()))
                .thenReturn(analista());

        mockMvc.perform(put("/api/v1/roles-proceso/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(datos("Analista", "Revisa solicitudes"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Analista"));
    }

    @Test
    void eliminar_sinUso_devuelve204() throws Exception {
        doNothing().when(rolProcesoService).eliminarRol(eq(1L), anyLong());

        mockMvc.perform(delete("/api/v1/roles-proceso/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void eliminar_enUso_devuelve400ConElDetalle() throws Exception {
        doThrow(new IllegalArgumentException(
                "No se puede eliminar el rol 'Analista' porque esta en uso en los procesos: Vacaciones"))
                .when(rolProcesoService).eliminarRol(eq(1L), anyLong());

        mockMvc.perform(delete("/api/v1/roles-proceso/1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value(
                        "No se puede eliminar el rol 'Analista' porque esta en uso en los procesos: Vacaciones"));
    }
}

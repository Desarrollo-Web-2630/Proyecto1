package com.proyecto1.thymeleaf.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyecto1.thymeleaf.dto.ProcesoDTO;
import com.proyecto1.thymeleaf.model.Proceso;
import com.proyecto1.thymeleaf.services.ProcesoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProcesoController.class)
class ProcesoControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private ProcesoService procesoService;

    @Test
    void listar_devuelveOkConLaListaDelService() throws Exception {
        Proceso proceso = new Proceso();
        proceso.setId(1L);
        proceso.setNombre("Solicitud de vacaciones");
        proceso.setEstado(Proceso.EstadoProceso.BORRADOR);

        when(procesoService.listarPorEmpresa(anyLong(), eq(false))).thenReturn(List.of(proceso));

        mockMvc.perform(get("/api/v1/procesos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Solicitud de vacaciones"));
    }

    @Test
    void buscar_conFiltros_devuelvePaginaDeResultados() throws Exception {
        Proceso proceso = new Proceso();
        proceso.setId(1L);
        proceso.setNombre("Compra de insumos");

        Page<Proceso> pagina = new PageImpl<>(List.of(proceso));
        when(procesoService.buscar(anyLong(), eq("compra"), isNull(), isNull(), eq(false), any()))
                .thenReturn(pagina);

        mockMvc.perform(get("/api/v1/procesos/buscar").param("nombre", "compra"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].nombre").value("Compra de insumos"));
    }

    @Test
    void obtener_inexistente_devuelve400() throws Exception {
        when(procesoService.obtenerPorIdYEmpresa(eq(99L), anyLong()))
                .thenThrow(new IllegalArgumentException("El proceso no existe o no pertenece a su empresa"));

        mockMvc.perform(get("/api/v1/procesos/99"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void crear_conDatosValidos_devuelve201() throws Exception {
        ProcesoDTO datos = new ProcesoDTO();
        datos.setNombre("Proceso de venta");
        datos.setDescripcion("Flujo de venta");
        datos.setCategoria("Comercial");

        Proceso creado = new Proceso();
        creado.setId(3L);
        creado.setNombre("Proceso de venta");
        creado.setEstado(Proceso.EstadoProceso.BORRADOR);

        when(procesoService.crearProceso(any(ProcesoDTO.class), anyLong())).thenReturn(creado);

        mockMvc.perform(post("/api/v1/procesos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(datos)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("BORRADOR"));
    }

    @Test
    void publicar_desdeBorrador_devuelveOkConEstadoPublicado() throws Exception {
        Proceso publicado = new Proceso();
        publicado.setId(1L);
        publicado.setEstado(Proceso.EstadoProceso.PUBLICADO);

        when(procesoService.publicarProceso(eq(1L), anyLong())).thenReturn(publicado);

        mockMvc.perform(post("/api/v1/procesos/1/publicar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("PUBLICADO"));
    }

    @Test
    void publicar_yaPublicado_devuelve400() throws Exception {
        when(procesoService.publicarProceso(eq(1L), anyLong()))
                .thenThrow(new IllegalArgumentException("El proceso ya está publicado"));

        mockMvc.perform(post("/api/v1/procesos/1/publicar"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void inactivar_devuelveOkConEstadoInactivo() throws Exception {
        Proceso inactivo = new Proceso();
        inactivo.setId(1L);
        inactivo.setEstado(Proceso.EstadoProceso.INACTIVO);

        when(procesoService.inactivarProceso(eq(1L), anyLong())).thenReturn(inactivo);

        mockMvc.perform(post("/api/v1/procesos/1/inactivar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("INACTIVO"));
    }

    @Test
    void eliminar_existente_devuelve204() throws Exception {
        mockMvc.perform(delete("/api/v1/procesos/1"))
                .andExpect(status().isNoContent());

        verify(procesoService).eliminarProceso(eq(1L), anyLong());
    }
}
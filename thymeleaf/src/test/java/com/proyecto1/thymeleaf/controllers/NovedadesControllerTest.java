package com.proyecto1.thymeleaf.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyecto1.thymeleaf.dto.ComparticionRequestDTO;
import com.proyecto1.thymeleaf.dto.ComparticionResponseDTO;
import com.proyecto1.thymeleaf.dto.EmpresaResumenDTO;
import com.proyecto1.thymeleaf.dto.HistorialCambioDTO;
import com.proyecto1.thymeleaf.dto.PermisoPoolDTO;
import com.proyecto1.thymeleaf.model.Usuario;
import com.proyecto1.thymeleaf.security.SesionDePrueba;
import com.proyecto1.thymeleaf.services.AuditoriaService;
import com.proyecto1.thymeleaf.services.ComparticionService;
import com.proyecto1.thymeleaf.services.PermisoPoolService;

@WebMvcTest({HistorialController.class, ComparticionController.class, PermisoPoolController.class})
@AutoConfigureMockMvc(addFilters = false)
class NovedadesControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private AuditoriaService auditoriaService;
    @MockBean private ComparticionService comparticionService;
    @MockBean private PermisoPoolService permisoPoolService;

    @BeforeEach
    void iniciarSesion() {
        SesionDePrueba.iniciar(1L, Usuario.RolAcceso.ADMIN);
    }

    @AfterEach
    void cerrarSesion() {
        SesionDePrueba.cerrar();
    }

    @Test
    void historialDeEmpresa_devuelveLosCambios() throws Exception {
        when(auditoriaService.historialDeEmpresa(1L)).thenReturn(List.of(new HistorialCambioDTO(
                1L, "Proceso", 7L, "CREAR", "Creado", "ana@demo.com", Instant.parse("2026-01-01T10:00:00Z"))));

        mockMvc.perform(get("/api/v1/historial"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].accion").value("CREAR"))
                .andExpect(jsonPath("$[0].usuarioCorreo").value("ana@demo.com"));
    }

    @Test
    void historialDeProceso_devuelveLosCambiosDelProceso() throws Exception {
        when(auditoriaService.historialDeEntidad("Proceso", 7L, 1L)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/procesos/7/historial"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void comparticion_listar_devuelveEmpresas() throws Exception {
        when(comparticionService.listar(7L, 1L)).thenReturn(new ComparticionResponseDTO(
                7L, true, List.of(new EmpresaResumenDTO(2L, "Invitada"))));

        mockMvc.perform(get("/api/v1/procesos/7/comparticion"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.compartido").value(true))
                .andExpect(jsonPath("$.empresasInvitadas[0].nombre").value("Invitada"));
    }

    @Test
    void comparticion_actualizar_delegaEnElServicio() throws Exception {
        when(comparticionService.actualizar(eq(7L), any(), eq(1L)))
                .thenReturn(new ComparticionResponseDTO(7L, false, List.of()));

        mockMvc.perform(put("/api/v1/procesos/7/comparticion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ComparticionRequestDTO(List.of(3L)))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.compartido").value(false));

        verify(comparticionService).actualizar(eq(7L), any(), eq(1L));
    }

    @Test
    void permisos_listar_devuelveLosRoles() throws Exception {
        when(permisoPoolService.listar(5L, 7L, 1L)).thenReturn(List.of(
                new PermisoPoolDTO("LECTURA", true, false, false)));

        mockMvc.perform(get("/api/v1/procesos/7/pools/5/permisos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].rolAcceso").value("LECTURA"))
                .andExpect(jsonPath("$[0].puedeCrear").value(true));
    }

    @Test
    void permisos_guardar_delegaEnElServicio() throws Exception {
        when(permisoPoolService.guardar(eq(5L), eq(7L), any(), eq(1L))).thenReturn(List.of());

        mockMvc.perform(put("/api/v1/procesos/7/pools/5/permisos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new PermisoPoolDTO("EDITOR", true, true, false))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());

        verify(permisoPoolService).guardar(eq(5L), eq(7L), any(), eq(1L));
    }
}

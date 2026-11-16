package com.proyecto1.thymeleaf.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyecto1.thymeleaf.dto.UsuarioDTO;
import com.proyecto1.thymeleaf.dto.UsuarioVistaDTO;
import com.proyecto1.thymeleaf.model.Usuario;
import com.proyecto1.thymeleaf.services.UsuarioService;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import com.proyecto1.thymeleaf.model.Usuario;
import com.proyecto1.thymeleaf.security.SesionDePrueba;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UsuarioController.class)
// Sin filtros de seguridad: aqui se prueba el controlador; las reglas se prueban en SeguridadIntegracionTest
@AutoConfigureMockMvc(addFilters = false)
class UsuarioControllerTest {

    @BeforeEach
    void iniciarSesion() {
        SesionDePrueba.iniciar(1L, Usuario.RolAcceso.ADMIN);
    }

    @AfterEach
    void cerrarSesion() {
        SesionDePrueba.cerrar();
    }

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private UsuarioService usuarioService;
    @MockBean private ModelMapper modelMapper;

    @Test
    void listar_devuelveOkConLaListaDelService() throws Exception {
        UsuarioVistaDTO vista = new UsuarioVistaDTO();
        vista.setId(1L);
        vista.setNombre("Ana");

        when(usuarioService.listarPorEmpresa(anyLong())).thenReturn(List.of(vista));

        mockMvc.perform(get("/api/v1/usuarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Ana"));
    }

    @Test
    void obtener_existente_devuelveOk() throws Exception {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        UsuarioVistaDTO vista = new UsuarioVistaDTO();
        vista.setId(1L);
        vista.setNombre("Ana");

        when(usuarioService.obtenerPorIdYEmpresa(eq(1L), anyLong())).thenReturn(usuario);
        when(modelMapper.map(usuario, UsuarioVistaDTO.class)).thenReturn(vista);

        mockMvc.perform(get("/api/v1/usuarios/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Ana"));
    }

    @Test
    void crear_conDatosValidos_devuelve201() throws Exception {
        UsuarioDTO datos = new UsuarioDTO();
        datos.setNombre("Ana Perez");
        datos.setCorreo("ana@demo.com");
        datos.setPassword("Clave1234");
        datos.setRolAcceso(Usuario.RolAcceso.EDITOR);

        Usuario creado = new Usuario();
        creado.setId(5L);
        UsuarioVistaDTO vista = new UsuarioVistaDTO();
        vista.setId(5L);
        vista.setNombre("Ana Perez");

        when(usuarioService.registrarUsuario(any(UsuarioDTO.class), anyLong())).thenReturn(creado);
        when(modelMapper.map(creado, UsuarioVistaDTO.class)).thenReturn(vista);

        mockMvc.perform(post("/api/v1/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(datos)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(5));
    }

    @Test
    void crear_conCorreoInvalido_devuelve400PorValidacionDeBean() throws Exception {
        UsuarioDTO datos = new UsuarioDTO();
        datos.setNombre("Ana Perez");
        datos.setCorreo("no-es-correo");
        datos.setPassword("Clave1234");
        datos.setRolAcceso(Usuario.RolAcceso.EDITOR);

        mockMvc.perform(post("/api/v1/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(datos)))
                .andExpect(status().isBadRequest());

        verify(usuarioService, never()).registrarUsuario(any(), anyLong());
    }

    @Test
    void eliminar_suPropiaCuenta_devuelve400SinLlamarAlService() throws Exception {
        mockMvc.perform(delete("/api/v1/usuarios/" + SesionDePrueba.USUARIO_ID))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("No puede eliminar su propia cuenta"));

        verify(usuarioService, never()).eliminarUsuario(anyLong(), anyLong());
    }

    @Test
    void cambiarRol_propio_devuelve400() throws Exception {
        mockMvc.perform(post("/api/v1/usuarios/" + SesionDePrueba.USUARIO_ID + "/rol").param("rolAcceso", "LECTURA"))
                .andExpect(status().isBadRequest());

        verify(usuarioService, never()).cambiarRol(anyLong(), any(), anyLong());
    }

    @Test
    void eliminar_existente_devuelve204() throws Exception {
        mockMvc.perform(delete("/api/v1/usuarios/1"))
                .andExpect(status().isNoContent());

        verify(usuarioService).eliminarUsuario(eq(1L), anyLong());
    }
    @Test
    void buscar_devuelvePaginaConEmpresaId() throws Exception {
        UsuarioVistaDTO vista = new UsuarioVistaDTO();
        vista.setId(1L);
        vista.setNombre("Ana");
        vista.setEmpresaId(3L);

        when(usuarioService.buscarPorEmpresa(anyLong(), any()))
                .thenReturn(new PageImpl<>(List.of(vista), PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/api/v1/usuarios/buscar?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].empresaId").value(3))
                .andExpect(jsonPath("$.totalElements").value(1));
    }
}
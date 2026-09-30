package com.proyecto1.thymeleaf.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyecto1.thymeleaf.dto.LoginDTO;
import com.proyecto1.thymeleaf.dto.UsuarioDTO;
import com.proyecto1.thymeleaf.dto.UsuarioVistaDTO;
import com.proyecto1.thymeleaf.model.Usuario;
import com.proyecto1.thymeleaf.services.UsuarioService;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UsuarioController.class)
class UsuarioControllerTest {

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
    void login_conCredencialesCorrectas_devuelveOk() throws Exception {
        LoginDTO login = new LoginDTO();
        login.setCorreo("ana@demo.com");
        login.setPassword("Clave1234");

        Usuario usuario = new Usuario();
        usuario.setId(1L);
        UsuarioVistaDTO vista = new UsuarioVistaDTO();
        vista.setId(1L);

        when(usuarioService.login("ana@demo.com", "Clave1234")).thenReturn(Optional.of(usuario));
        when(modelMapper.map(usuario, UsuarioVistaDTO.class)).thenReturn(vista);

        mockMvc.perform(post("/api/v1/usuarios/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk());
    }

    @Test
    void login_conCredencialesIncorrectas_devuelve401SinCuerpo() throws Exception {
        LoginDTO login = new LoginDTO();
        login.setCorreo("ana@demo.com");
        login.setPassword("Mala12345");

        when(usuarioService.login("ana@demo.com", "Mala12345")).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/v1/usuarios/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void eliminar_existente_devuelve204() throws Exception {
        mockMvc.perform(delete("/api/v1/usuarios/1"))
                .andExpect(status().isNoContent());

        verify(usuarioService).eliminarUsuario(eq(1L), anyLong());
    }
}
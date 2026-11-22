package com.proyecto1.thymeleaf.services;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.proyecto1.thymeleaf.dto.PermisoPoolDTO;
import com.proyecto1.thymeleaf.model.PermisoPool;
import com.proyecto1.thymeleaf.model.Pool;
import com.proyecto1.thymeleaf.model.Usuario;
import com.proyecto1.thymeleaf.repository.PermisoPoolRepository;
import com.proyecto1.thymeleaf.security.UsuarioAutenticado;

@ExtendWith(MockitoExtension.class)
class PermisoPoolServiceTest {

    @Mock private PermisoPoolRepository permisoRepository;
    @Mock private PoolService poolService;
    @Mock private AuditoriaService auditoriaService;

    @InjectMocks
    private PermisoPoolService permisoPoolService;

    private Pool pool;

    @BeforeEach
    void setUp() {
        pool = new Pool();
        pool.setId(5L);
    }

    @AfterEach
    void limpiar() {
        SecurityContextHolder.clearContext();
    }

    private void autenticar(Usuario.RolAcceso rol) {
        UsuarioAutenticado usuario = new UsuarioAutenticado(1L, "juan@correo.com", "Juan", "clave",
                1L, rol, true);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities()));
    }

    @Test
    void guardar_creaElPermisoYLoAudita() {
        PermisoPoolDTO datos = new PermisoPoolDTO("EDITOR", true, false, true);
        when(poolService.obtenerPorIdYEmpresa(5L, 7L, 1L)).thenReturn(pool);
        when(permisoRepository.findByPoolIdAndRolAcceso(5L, "EDITOR")).thenReturn(Optional.empty());
        when(permisoRepository.findByPoolId(5L)).thenReturn(List.of());

        List<PermisoPoolDTO> resultado = permisoPoolService.guardar(5L, 7L, datos, 1L);

        assertTrue(resultado.isEmpty());
        verify(permisoRepository).save(any());
        verify(auditoriaService).registrar(eq("Pool"), eq(5L), eq("PERMISOS"), any());
    }

    @Test
    void guardar_rolInvalido_lanzaExcepcion() {
        PermisoPoolDTO datos = new PermisoPoolDTO("INVITADO", true, true, true);
        when(poolService.obtenerPorIdYEmpresa(5L, 7L, 1L)).thenReturn(pool);

        assertThrows(IllegalArgumentException.class,
                () -> permisoPoolService.guardar(5L, 7L, datos, 1L));
    }

    @Test
    void verificar_adminSiemprePuede() {
        autenticar(Usuario.RolAcceso.ADMIN);

        permisoPoolService.verificar(pool, "ELIMINAR");
    }

    @Test
    void verificar_editorSinPermisoExplicitoTieneAccesoPorDefecto() {
        autenticar(Usuario.RolAcceso.EDITOR);
        when(permisoRepository.findByPoolIdAndRolAcceso(5L, "EDITOR")).thenReturn(Optional.empty());

        permisoPoolService.verificar(pool, "CREAR");
    }

    @Test
    void verificar_lecturaSinPermisoExplicito_noPuedeEscribir() {
        autenticar(Usuario.RolAcceso.LECTURA);
        when(permisoRepository.findByPoolIdAndRolAcceso(5L, "LECTURA")).thenReturn(Optional.empty());

        assertThrows(AccessDeniedException.class, () -> permisoPoolService.verificar(pool, "EDITAR"));
    }

    @Test
    void verificar_lectorConPermisoExplicitoPuedeLeer() {
        autenticar(Usuario.RolAcceso.LECTURA);
        when(permisoRepository.findByPoolIdAndRolAcceso(5L, "LECTURA"))
                .thenReturn(Optional.of(new PermisoPool(1L, pool, "LECTURA", true, false, false)));

        permisoPoolService.verificar(pool, "CREAR");
    }
}

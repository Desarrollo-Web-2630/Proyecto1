package com.proyecto1.thymeleaf.services;

import com.proyecto1.thymeleaf.model.ElementoConectable;
import com.proyecto1.thymeleaf.model.Empresa;
import com.proyecto1.thymeleaf.model.Proceso;
import com.proyecto1.thymeleaf.repository.ElementoConectableRepository;
import com.proyecto1.thymeleaf.repository.ProcesoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ElementoConectableServiceTest {

    @Mock private ElementoConectableRepository elementoRepository;
    @Mock private ProcesoRepository procesoRepository;

    @InjectMocks
    private ElementoConectableService elementoConectableService;

    static class ElementoDePrueba extends ElementoConectable {
    }

    private Proceso proceso;

    @BeforeEach
    void setUp() {
        Empresa empresa = new Empresa();
        empresa.setId(1L);
        proceso = new Proceso();
        proceso.setId(1L);
        proceso.setEmpresa(empresa);
    }

    @Test
    void listarElementosPorProcesoYEmpresa_delegaAlRepositorio() {
        when(procesoRepository.findByIdAndEmpresaId(1L, 1L)).thenReturn(Optional.of(proceso));
        when(elementoRepository.findByProcesoId(1L)).thenReturn(List.of(new ElementoDePrueba()));

        assertEquals(1, elementoConectableService.listarElementosPorProcesoYEmpresa(1L, 1L).size());
    }

    @Test
    void listarElementosPorProcesoYEmpresa_conProcesoDeOtraEmpresa_lanzaExcepcion() {
        when(procesoRepository.findByIdAndEmpresaId(1L, 1L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class,
                () -> elementoConectableService.listarElementosPorProcesoYEmpresa(1L, 1L));
    }

    @Test
    void actualizarPosiciones_conElementoExistente_actualizaXY() {
        ElementoDePrueba elemento = new ElementoDePrueba();
        elemento.setId(5L);
        elemento.setPosicionX(0);
        elemento.setPosicionY(0);

        when(elementoRepository.findByIdAndProcesoEmpresaId(5L, 1L)).thenReturn(Optional.of(elemento));
        when(elementoRepository.save(any(ElementoConectable.class))).thenAnswer(inv -> inv.getArgument(0));

        elementoConectableService.actualizarPosiciones(5L, 30, 40, 1L);

        assertEquals(30, elemento.getPosicionX());
        assertEquals(40, elemento.getPosicionY());
        verify(elementoRepository).save(elemento);
    }

    @Test
    void actualizarPosiciones_conElementoInexistente_lanzaExcepcion() {
        when(elementoRepository.findByIdAndProcesoEmpresaId(5L, 1L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class,
                () -> elementoConectableService.actualizarPosiciones(5L, 30, 40, 1L));
    }

    @Test
    void obtenerPorIdYEmpresa_inexistente_lanzaExcepcion() {
        when(elementoRepository.findByIdAndProcesoEmpresaId(5L, 1L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class,
                () -> elementoConectableService.obtenerPorIdYEmpresa(5L, 1L));
    }

    @Test
    void eliminarElemento_existente_invocaDelete() {
        ElementoDePrueba elemento = new ElementoDePrueba();
        elemento.setId(5L);

        when(elementoRepository.findByIdAndProcesoEmpresaId(5L, 1L)).thenReturn(Optional.of(elemento));

        elementoConectableService.eliminarElemento(5L, 1L);

        verify(elementoRepository).delete(elemento);
    }
}
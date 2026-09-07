package com.proyecto1.thymeleaf.services;

import com.proyecto1.thymeleaf.model.ElementoConectable;
import com.proyecto1.thymeleaf.repository.ElementoConectableRepository;
import com.proyecto1.thymeleaf.repository.ProcesoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ElementoConectableService {

    private final ElementoConectableRepository elementoRepository;
    private final ProcesoRepository procesoRepository;

    public ElementoConectableService(ElementoConectableRepository elementoRepository, ProcesoRepository procesoRepository) {
        this.elementoRepository = elementoRepository;
        this.procesoRepository = procesoRepository;
    }

    @Transactional(readOnly = true)
    public List<ElementoConectable> listarElementosPorProcesoYEmpresa(Long procesoId, Long empresaId) {
        validarAccesoProceso(procesoId, empresaId);
        return elementoRepository.findByProcesoId(procesoId);
    }

    public void actualizarPosiciones(Long elementoId, Integer nuevaX, Integer nuevaY, Long empresaId) {
        ElementoConectable elemento = elementoRepository.findByIdAndProcesoEmpresaId(elementoId, empresaId)
                .orElseThrow(() -> new IllegalArgumentException("Elemento no encontrado o sin acceso"));
        
        elemento.setPosicionX(nuevaX);
        elemento.setPosicionY(nuevaY);
        elementoRepository.save(elemento);
    }

    private void validarAccesoProceso(Long procesoId, Long empresaId) {
        procesoRepository.findByIdAndEmpresaId(procesoId, empresaId)
                .orElseThrow(() -> new IllegalArgumentException("Proceso no encontrado o sin acceso"));
    }
    // Obtener por ID validando la empresa
    @Transactional(readOnly = true)
    public ElementoConectable obtenerPorIdYEmpresa(Long id, Long empresaId) {
        return elementoRepository.findByIdAndProcesoEmpresaId(id, empresaId)
                .orElseThrow(() -> new IllegalArgumentException("Elemento no encontrado o sin autorización"));
    }

    // Eliminar un elemento conectable
    public void eliminarElemento(Long id, Long empresaId) {
        ElementoConectable elemento = obtenerPorIdYEmpresa(id, empresaId);
        elementoRepository.delete(elemento);
    }
}
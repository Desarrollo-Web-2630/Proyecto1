package com.proyecto1.thymeleaf.services;

import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.proyecto1.thymeleaf.dto.HistorialCambioDTO;
import com.proyecto1.thymeleaf.model.HistorialCambio;
import com.proyecto1.thymeleaf.repository.HistorialCambioRepository;
import com.proyecto1.thymeleaf.util.EmpresaActual;

@Service
public class AuditoriaService {

    private final HistorialCambioRepository historialRepository;

    public AuditoriaService(HistorialCambioRepository historialRepository) {
        this.historialRepository = historialRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrar(String entidad, Long entidadId, String accion, String descripcion) {
        Long empresaId;
        String correo;
        try {
            empresaId = EmpresaActual.id();
            correo = EmpresaActual.usuario().getCorreo();
        } catch (RuntimeException sinSesion) {

            empresaId = 0L;
            correo = "sistema";
        }
        HistorialCambio cambio = new HistorialCambio(null, empresaId, entidad, entidadId, accion,
                descripcion == null ? null : descripcion.substring(0, Math.min(descripcion.length(), 1000)),
                correo, Instant.now());
        historialRepository.save(cambio);
    }

    @Transactional(readOnly = true)
    public List<HistorialCambioDTO> historialDeEntidad(String entidad, Long entidadId, Long empresaId) {
        return historialRepository.findByEmpresaYEntidad(empresaId, entidad, entidadId).stream()
                .map(HistorialCambioDTO::desde)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<HistorialCambioDTO> historialDeEmpresa(Long empresaId) {
        return historialRepository.findByEmpresa(empresaId).stream()
                .map(HistorialCambioDTO::desde)
                .toList();
    }
}

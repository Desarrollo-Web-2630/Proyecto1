package com.proyecto1.thymeleaf.services;

import com.proyecto1.thymeleaf.model.Gateway;
import com.proyecto1.thymeleaf.model.Proceso;
import com.proyecto1.thymeleaf.repository.GatewayRepository;
import com.proyecto1.thymeleaf.repository.ProcesoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class GatewayService {

    private final GatewayRepository gatewayRepository;
    private final ProcesoRepository procesoRepository;

    public GatewayService(GatewayRepository gatewayRepository, ProcesoRepository procesoRepository) {
        this.gatewayRepository = gatewayRepository;
        this.procesoRepository = procesoRepository;
    }

    // 1. Listar gateways de un proceso verificando la empresa
    @Transactional(readOnly = true)
    public List<Gateway> listarPorProcesoYEmpresa(Long procesoId, Long empresaId) {
        validarAccesoProceso(procesoId, empresaId);
        return gatewayRepository.findByProcesoId(procesoId);
    }

    // 2. Obtener un gateway especifico verificando que pertenezca a la empresa
    @Transactional(readOnly = true)
    public Gateway obtenerPorIdYEmpresa(Long id, Long empresaId) {
        return gatewayRepository.findByIdAndProcesoEmpresaId(id, empresaId)
                .orElseThrow(() -> new IllegalArgumentException("El gateway no existe o no pertenece a su empresa"));
    }

    // 3. Crear un nuevo gateway dentro de un proceso autorizado
    public Gateway crearGateway(Gateway gateway, Long procesoId, Long empresaId) {
        Proceso proceso = validarAccesoProceso(procesoId, empresaId);
        gateway.setProceso(proceso);
        return gatewayRepository.save(gateway);
    }

    // 4. Actualizar un gateway existente
    public Gateway actualizarGateway(Long id, Gateway gatewayDetalles, Long empresaId) {
        Gateway gatewayExistente = obtenerPorIdYEmpresa(id, empresaId);
        
        gatewayExistente.setNombre(gatewayDetalles.getNombre());
        gatewayExistente.setTipo(gatewayDetalles.getTipo());

        return gatewayRepository.save(gatewayExistente);
    }

    // 5. Eliminar (soft delete via @SQLDelete)
    public void eliminarGateway(Long id, Long empresaId) {
        Gateway gateway = obtenerPorIdYEmpresa(id, empresaId);
        gatewayRepository.delete(gateway);
    }

    // Método auxiliar privado para validar que el proceso pertenece a la empresa
    private Proceso validarAccesoProceso(Long procesoId, Long empresaId) {
        return procesoRepository.findByIdAndEmpresaId(procesoId, empresaId)
                .orElseThrow(() -> new IllegalArgumentException("Proceso no encontrado o no pertenece a su empresa"));
    }
}
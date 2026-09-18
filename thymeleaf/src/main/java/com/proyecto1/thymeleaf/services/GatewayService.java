package com.proyecto1.thymeleaf.services;

import com.proyecto1.thymeleaf.dto.GatewayDTO;
import com.proyecto1.thymeleaf.model.Gateway;
import com.proyecto1.thymeleaf.model.Proceso;
import com.proyecto1.thymeleaf.repository.GatewayRepository;
import com.proyecto1.thymeleaf.repository.ProcesoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Logica de negocio de los gateways (HU-14 crear, HU-15 editar, HU-16
 * eliminar).
 */
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
    public Gateway crearGateway(GatewayDTO datos, Long procesoId, Long empresaId) {
        Proceso proceso = validarAccesoProceso(procesoId, empresaId);
        validarDatosObligatorios(datos);

        Gateway gateway = new Gateway();
        gateway.setNombre(datos.getNombre().trim());
        gateway.setTipo(datos.getTipo());
        gateway.setProceso(proceso);
        gateway.setStatus(0);

        return gatewayRepository.save(gateway);
    }

    // 4. Actualizar un gateway existente
    public Gateway actualizarGateway(Long id, GatewayDTO datos, Long empresaId) {
        Gateway gatewayExistente = obtenerPorIdYEmpresa(id, empresaId);
        validarDatosObligatorios(datos);

        gatewayExistente.setNombre(datos.getNombre().trim());
        gatewayExistente.setTipo(datos.getTipo());

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

    private void validarDatosObligatorios(GatewayDTO datos) {
        if (datos.getNombre() == null || datos.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre del gateway es obligatorio");
        }
        if (datos.getTipo() == null) {
            throw new IllegalArgumentException("El tipo de gateway es obligatorio");
        }
    }
}

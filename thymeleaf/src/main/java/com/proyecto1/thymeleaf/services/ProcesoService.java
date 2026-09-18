package com.proyecto1.thymeleaf.services;

import com.proyecto1.thymeleaf.dto.ProcesoRequestDTO;
import com.proyecto1.thymeleaf.dto.ProcesoResponseDTO;
import com.proyecto1.thymeleaf.model.Empresa;
import com.proyecto1.thymeleaf.model.Proceso;
import com.proyecto1.thymeleaf.repository.EmpresaRepository;
import com.proyecto1.thymeleaf.repository.ProcesoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Logica de negocio de los procesos (HU-04 Crear proceso).
 *
 * Todas las operaciones reciben el empresaId del usuario autenticado para
 * garantizar que una empresa nunca alcance los procesos de otra.
 */
@Service
@Transactional
public class ProcesoService {

    private final ProcesoRepository procesoRepository;
    private final EmpresaRepository empresaRepository;

    public ProcesoService(ProcesoRepository procesoRepository, EmpresaRepository empresaRepository) {
        this.procesoRepository = procesoRepository;
        this.empresaRepository = empresaRepository;
    }

    // 1. Listar los procesos de la empresa del usuario autenticado
    @Transactional(readOnly = true)
    public List<ProcesoResponseDTO> listarPorEmpresa(Long empresaId) {
        return procesoRepository.findByEmpresaId(empresaId)
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    // 2. Obtener un proceso verificando que pertenezca a la empresa
    @Transactional(readOnly = true)
    public ProcesoResponseDTO obtenerPorIdYEmpresa(Long id, Long empresaId) {
        Proceso proceso = procesoRepository.findByIdAndEmpresaId(id, empresaId)
                .orElseThrow(() -> new IllegalArgumentException("El proceso no existe o no pertenece a su empresa"));
        return toResponseDTO(proceso);
    }

    // 3. Crear un proceso asociado a la empresa del usuario autenticado
    public ProcesoResponseDTO crearProceso(ProcesoRequestDTO request, Long empresaId) {
        validarDatosObligatorios(request);

        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new IllegalArgumentException("La empresa no existe"));

        String nombre = request.getNombre().trim();
        if (procesoRepository.existsByNombreAndEmpresaId(nombre, empresaId)) {
            throw new IllegalArgumentException("Ya existe un proceso llamado '" + nombre + "' en la empresa");
        }

        Proceso proceso = new Proceso();
        proceso.setNombre(nombre);
        proceso.setDescripcion(request.getDescripcion().trim());
        proceso.setCategoria(request.getCategoria().trim());
        proceso.setEmpresa(empresa);
        proceso.setEstado(Proceso.EstadoProceso.BORRADOR);

        Proceso guardado = procesoRepository.save(proceso);
        return toResponseDTO(guardado);
    }

    // 4. Actualizar la informacion basica de un proceso
    public ProcesoResponseDTO actualizarProceso(Long id, ProcesoRequestDTO request, Long empresaId) {
        Proceso procesoExistente = procesoRepository.findByIdAndEmpresaId(id, empresaId)
                .orElseThrow(() -> new IllegalArgumentException("El proceso no existe o no pertenece a su empresa"));

        String nombre = request.getNombre().trim();
        if (procesoRepository.existsByNombreAndEmpresaIdAndIdNot(nombre, empresaId, id)) {
            throw new IllegalArgumentException("Ya existe un proceso llamado '" + nombre + "' en la empresa");
        }

        procesoExistente.setNombre(nombre);
        procesoExistente.setDescripcion(request.getDescripcion().trim());
        procesoExistente.setCategoria(request.getCategoria().trim());

        Proceso guardado = procesoRepository.save(procesoExistente);
        return toResponseDTO(guardado);
    }

    // 5. Pasar el proceso de borrador a publicado
    public ProcesoResponseDTO publicarProceso(Long id, Long empresaId) {
        Proceso proceso = procesoRepository.findByIdAndEmpresaId(id, empresaId)
                .orElseThrow(() -> new IllegalArgumentException("El proceso no existe o no pertenece a su empresa"));

        if (proceso.getEstado() == Proceso.EstadoProceso.PUBLICADO) {
            throw new IllegalArgumentException("El proceso ya esta publicado");
        }

        proceso.setEstado(Proceso.EstadoProceso.PUBLICADO);
        Proceso guardado = procesoRepository.save(proceso);
        return toResponseDTO(guardado);
    }

    // 6. Eliminar (borrado logico via @SQLDelete)
    public void eliminarProceso(Long id, Long empresaId) {
        Proceso proceso = procesoRepository.findByIdAndEmpresaId(id, empresaId)
                .orElseThrow(() -> new IllegalArgumentException("El proceso no existe o no pertenece a su empresa"));
        procesoRepository.delete(proceso);
    }

    private void validarDatosObligatorios(ProcesoRequestDTO request) {
        if (request.getNombre() == null || request.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre del proceso es obligatorio");
        }
        if (request.getDescripcion() == null || request.getDescripcion().isBlank()) {
            throw new IllegalArgumentException("La descripcion del proceso es obligatoria");
        }
        if (request.getCategoria() == null || request.getCategoria().isBlank()) {
            throw new IllegalArgumentException("La categoria del proceso es obligatoria");
        }
    }

    private ProcesoResponseDTO toResponseDTO(Proceso proceso) {
        return new ProcesoResponseDTO(
                proceso.getId(),
                proceso.getNombre(),
                proceso.getDescripcion(),
                proceso.getCategoria(),
                proceso.getEstado().name()
        );
    }
}

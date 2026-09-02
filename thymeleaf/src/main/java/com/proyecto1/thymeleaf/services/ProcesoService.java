package com.proyecto1.thymeleaf.services;

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
    public List<Proceso> listarPorEmpresa(Long empresaId) {
        return procesoRepository.findByEmpresaId(empresaId);
    }

    // 2. Obtener un proceso verificando que pertenezca a la empresa
    @Transactional(readOnly = true)
    public Proceso obtenerPorIdYEmpresa(Long id, Long empresaId) {
        return procesoRepository.findByIdAndEmpresaId(id, empresaId)
                .orElseThrow(() -> new IllegalArgumentException("El proceso no existe o no pertenece a su empresa"));
    }

    // 3. Crear un proceso asociado a la empresa del usuario autenticado
    public Proceso crearProceso(Proceso proceso, Long empresaId) {
        validarDatosObligatorios(proceso);

        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new IllegalArgumentException("La empresa no existe"));

        String nombre = proceso.getNombre().trim();
        if (procesoRepository.existsByNombreAndEmpresaId(nombre, empresaId)) {
            throw new IllegalArgumentException("Ya existe un proceso llamado '" + nombre + "' en la empresa");
        }

        proceso.setNombre(nombre);
        proceso.setDescripcion(proceso.getDescripcion().trim());
        proceso.setCategoria(proceso.getCategoria().trim());
        proceso.setEmpresa(empresa);
        // El proceso siempre nace en borrador, sin importar que estado llegue del formulario
        proceso.setEstado(Proceso.EstadoProceso.BORRADOR);

        return procesoRepository.save(proceso);
    }

    // 4. Actualizar la informacion basica de un proceso
    public Proceso actualizarProceso(Long id, Proceso procesoDetalles, Long empresaId) {
        Proceso procesoExistente = obtenerPorIdYEmpresa(id, empresaId);
        validarDatosObligatorios(procesoDetalles);

        String nombre = procesoDetalles.getNombre().trim();
        if (procesoRepository.existsByNombreAndEmpresaIdAndIdNot(nombre, empresaId, id)) {
            throw new IllegalArgumentException("Ya existe un proceso llamado '" + nombre + "' en la empresa");
        }

        procesoExistente.setNombre(nombre);
        procesoExistente.setDescripcion(procesoDetalles.getDescripcion().trim());
        procesoExistente.setCategoria(procesoDetalles.getCategoria().trim());

        return procesoRepository.save(procesoExistente);
    }

    // 5. Pasar el proceso de borrador a publicado
    public Proceso publicarProceso(Long id, Long empresaId) {
        Proceso proceso = obtenerPorIdYEmpresa(id, empresaId);

        if (proceso.getEstado() == Proceso.EstadoProceso.PUBLICADO) {
            throw new IllegalArgumentException("El proceso ya esta publicado");
        }

        proceso.setEstado(Proceso.EstadoProceso.PUBLICADO);
        return procesoRepository.save(proceso);
    }

    // 6. Eliminar (borrado logico via @SQLDelete)
    public void eliminarProceso(Long id, Long empresaId) {
        Proceso proceso = obtenerPorIdYEmpresa(id, empresaId);
        procesoRepository.delete(proceso);
    }

    // Nombre, descripcion y categoria son los datos que pide la HU-04
    private void validarDatosObligatorios(Proceso proceso) {
        if (proceso.getNombre() == null || proceso.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre del proceso es obligatorio");
        }
        if (proceso.getDescripcion() == null || proceso.getDescripcion().isBlank()) {
            throw new IllegalArgumentException("La descripcion del proceso es obligatoria");
        }
        if (proceso.getCategoria() == null || proceso.getCategoria().isBlank()) {
            throw new IllegalArgumentException("La categoria del proceso es obligatoria");
        }
    }
}

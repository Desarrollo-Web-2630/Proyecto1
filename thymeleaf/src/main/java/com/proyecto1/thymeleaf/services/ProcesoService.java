package com.proyecto1.thymeleaf.services;

import com.proyecto1.thymeleaf.dto.ProcesoDTO;
import com.proyecto1.thymeleaf.model.Empresa;
import com.proyecto1.thymeleaf.model.Proceso;
import com.proyecto1.thymeleaf.repository.EmpresaRepository;
import com.proyecto1.thymeleaf.repository.ProcesoRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Logica de negocio de los procesos (HU-04 crear, HU-05 editar, HU-06
 * eliminar, HU-07 consultar).
 *
 * Las escrituras reciben ProcesoDTO y no la entidad: asi el formulario no
 * puede tocar campos que no le corresponden, como el estado o la empresa.
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

    // 1. Listar los procesos de la empresa: activos por defecto (HU-07)
    @Transactional(readOnly = true)
    public List<Proceso> listarPorEmpresa(Long empresaId) {
        return listarPorEmpresa(empresaId, false);
    }

    @Transactional(readOnly = true)
    public List<Proceso> listarPorEmpresa(Long empresaId, boolean incluirInactivos) {
        return incluirInactivos
                ? procesoRepository.findByEmpresaId(empresaId)
                : procesoRepository.findByEmpresaIdAndEstadoNot(empresaId, Proceso.EstadoProceso.INACTIVO);
    }

    /**
     * HU-07: busqueda por nombre, filtros por estado y categoria, y
     * paginacion. Cada filtro se aplica solo si viene informado; el
     * aislamiento por empresa se aplica siempre.
     */
    @Transactional(readOnly = true)
    public Page<Proceso> buscar(Long empresaId, String nombre, Proceso.EstadoProceso estado,
                                String categoria, boolean incluirInactivos, Pageable pageable) {
        Specification<Proceso> filtros = (raiz, consulta, cb) -> {
            List<Predicate> condiciones = new ArrayList<>();
            condiciones.add(cb.equal(raiz.get("empresa").get("id"), empresaId));

            if (nombre != null && !nombre.isBlank()) {
                condiciones.add(cb.like(cb.lower(raiz.get("nombre")), "%" + nombre.trim().toLowerCase() + "%"));
            }
            if (estado != null) {
                condiciones.add(cb.equal(raiz.get("estado"), estado));
            } else if (!incluirInactivos) {
                condiciones.add(cb.notEqual(raiz.get("estado"), Proceso.EstadoProceso.INACTIVO));
            }
            if (categoria != null && !categoria.isBlank()) {
                condiciones.add(cb.equal(cb.lower(raiz.get("categoria")), categoria.trim().toLowerCase()));
            }
            return cb.and(condiciones.toArray(new Predicate[0]));
        };
        return procesoRepository.findAll(filtros, pageable);
    }

    // 2. Obtener un proceso verificando que pertenezca a la empresa
    @Transactional(readOnly = true)
    public Proceso obtenerPorIdYEmpresa(Long id, Long empresaId) {
        return procesoRepository.findByIdAndEmpresaId(id, empresaId)
                .orElseThrow(() -> new IllegalArgumentException("El proceso no existe o no pertenece a su empresa"));
    }

    // 3. Crear un proceso asociado a la empresa del usuario autenticado
    public Proceso crearProceso(ProcesoDTO datos, Long empresaId) {
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new IllegalArgumentException("La empresa no existe"));

        String nombre = normalizar(datos.getNombre(), "El nombre del proceso es obligatorio");
        if (procesoRepository.existsByNombreAndEmpresaId(nombre, empresaId)) {
            throw new IllegalArgumentException("Ya existe un proceso llamado '" + nombre + "' en la empresa");
        }

        Proceso proceso = new Proceso();
        proceso.setNombre(nombre);
        proceso.setDescripcion(normalizar(datos.getDescripcion(), "La descripción del proceso es obligatoria"));
        proceso.setCategoria(normalizar(datos.getCategoria(), "La categoría del proceso es obligatoria"));
        proceso.setEmpresa(empresa);
        // El proceso siempre nace en borrador: el formulario no decide el estado
        proceso.setEstado(Proceso.EstadoProceso.BORRADOR);

        return procesoRepository.save(proceso);
    }

    // 4. Actualizar la informacion basica de un proceso
    public Proceso actualizarProceso(Long id, ProcesoDTO datos, Long empresaId) {
        Proceso procesoExistente = obtenerPorIdYEmpresa(id, empresaId);

        String nombre = normalizar(datos.getNombre(), "El nombre del proceso es obligatorio");
        if (procesoRepository.existsByNombreAndEmpresaIdAndIdNot(nombre, empresaId, id)) {
            throw new IllegalArgumentException("Ya existe un proceso llamado '" + nombre + "' en la empresa");
        }

        procesoExistente.setNombre(nombre);
        procesoExistente.setDescripcion(normalizar(datos.getDescripcion(), "La descripción del proceso es obligatoria"));
        procesoExistente.setCategoria(normalizar(datos.getCategoria(), "La categoría del proceso es obligatoria"));

        return procesoRepository.save(procesoExistente);
    }

    // 5. Pasar el proceso a publicado
    public Proceso publicarProceso(Long id, Long empresaId) {
        Proceso proceso = obtenerPorIdYEmpresa(id, empresaId);

        if (proceso.getEstado() == Proceso.EstadoProceso.PUBLICADO) {
            throw new IllegalArgumentException("El proceso ya está publicado");
        }

        proceso.setEstado(Proceso.EstadoProceso.PUBLICADO);
        return procesoRepository.save(proceso);
    }

    // 6. Inactivar: sale de los listados por defecto, pero se conserva
    public Proceso inactivarProceso(Long id, Long empresaId) {
        Proceso proceso = obtenerPorIdYEmpresa(id, empresaId);
        proceso.setEstado(Proceso.EstadoProceso.INACTIVO);
        return procesoRepository.save(proceso);
    }

    // 7. Reactivar: vuelve a borrador para poder seguir editandolo
    public Proceso reactivarProceso(Long id, Long empresaId) {
        Proceso proceso = obtenerPorIdYEmpresa(id, empresaId);
        if (proceso.getEstado() != Proceso.EstadoProceso.INACTIVO) {
            throw new IllegalArgumentException("El proceso no está inactivo");
        }
        proceso.setEstado(Proceso.EstadoProceso.BORRADOR);
        return procesoRepository.save(proceso);
    }

    // 8. Eliminar (borrado logico via @SQLDelete)
    public void eliminarProceso(Long id, Long empresaId) {
        Proceso proceso = obtenerPorIdYEmpresa(id, empresaId);
        procesoRepository.delete(proceso);
    }

    /**
     * El DTO ya trae las anotaciones de validacion, pero el servicio no puede
     * confiar en que siempre lo llamen desde un formulario validado.
     */
    private String normalizar(String valor, String mensajeSiFalta) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(mensajeSiFalta);
        }
        return valor.trim();
    }
}

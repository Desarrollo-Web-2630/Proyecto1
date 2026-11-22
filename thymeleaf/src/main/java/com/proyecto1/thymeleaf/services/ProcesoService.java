package com.proyecto1.thymeleaf.services;

import com.proyecto1.thymeleaf.dto.ProcesoDTO;
import com.proyecto1.thymeleaf.model.Empresa;
import com.proyecto1.thymeleaf.model.Proceso;
import com.proyecto1.thymeleaf.repository.ProcesoRepository;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// Logica de negocio de los procesos (HU-04 crear, HU-05 editar, HU-06 eliminar, HU-07 consultar).

@Service
@Transactional
public class ProcesoService {

    private final ProcesoRepository procesoRepository;
    private final EmpresaService empresaService;
    private final AuditoriaService auditoriaService;
    private final GatewayService gatewayService;
    private final AccesoProcesoService accesoProcesoService;

    public ProcesoService(ProcesoRepository procesoRepository, EmpresaService empresaService,
                          AuditoriaService auditoriaService, @Lazy GatewayService gatewayService,
                          AccesoProcesoService accesoProcesoService) {
        this.procesoRepository = procesoRepository;
        this.empresaService = empresaService;
        this.auditoriaService = auditoriaService;
        this.gatewayService = gatewayService;
        this.accesoProcesoService = accesoProcesoService;
    }

    // 1. Listar los procesos de la empresa: activos por defecto (HU-07)
    @Transactional(readOnly = true)
    public List<Proceso> listarPorEmpresa(Long empresaId) {
        return listarPorEmpresa(empresaId, false);
    }

    @Transactional(readOnly = true)
    public List<Proceso> listarPorEmpresa(Long empresaId, boolean incluirInactivos) {
        List<Proceso> propios = new ArrayList<>(incluirInactivos
                ? procesoRepository.findByEmpresaId(empresaId)
                : procesoRepository.findByEmpresaIdAndEstadoNot(empresaId, Proceso.EstadoProceso.INACTIVO));
        Set<Long> conocidos = new LinkedHashSet<>();
        for (Proceso proceso : propios) {
            conocidos.add(proceso.getId());
        }
        for (Long id : accesoProcesoService.idsAccesibles(empresaId)) {
            if (conocidos.add(id)) {
                procesoRepository.findById(id)
                        .filter(p -> incluirInactivos || p.getEstado() != Proceso.EstadoProceso.INACTIVO)
                        .ifPresent(propios::add);
            }
        }
        return propios;
    }

    @Transactional(readOnly = true)
    public Page<Proceso> buscar(Long empresaId, String nombre, Proceso.EstadoProceso estado,
                                String categoria, boolean incluirInactivos, Pageable pageable) {
        return procesoRepository.buscar(empresaId, nombre, estado, incluirInactivos,
                Proceso.EstadoProceso.INACTIVO, categoria, pageable);
    }

    // 2. Obtener un proceso verificando que pertenezca a la empresa
    @Transactional(readOnly = true)
    public Proceso obtenerPorIdYEmpresa(Long id, Long empresaId) {
        return procesoRepository.findByIdAndEmpresaId(id, empresaId)
                .orElseThrow(() -> new IllegalArgumentException("El proceso no existe o no pertenece a su empresa"));
    }

    @Transactional(readOnly = true)
    public Proceso obtenerParaLectura(Long id, Long empresaId) {
        return accesoProcesoService.verificarLectura(id, empresaId);
    }

    @Transactional(readOnly = true)
    public boolean esSoloLectura(Long id, Long empresaId) {
        return accesoProcesoService.esSoloLectura(id, empresaId);
    }

    // 3. Crear un proceso asociado a la empresa del usuario autenticado
    public Proceso crearProceso(ProcesoDTO datos, Long empresaId) {
        Empresa empresa = empresaService.obtenerPorId(empresaId);

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

        Proceso guardado = procesoRepository.save(proceso);
        auditoriaService.registrar("Proceso", guardado.getId(), "CREAR", "Creado: " + guardado.getNombre());
        return guardado;
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

        Proceso guardado = procesoRepository.save(procesoExistente);
        auditoriaService.registrar("Proceso", guardado.getId(), "EDITAR", "Editado: " + guardado.getNombre());
        return guardado;
    }

    // 5. Pasar el proceso a publicado
    public Proceso publicarProceso(Long id, Long empresaId) {
        Proceso proceso = obtenerPorIdYEmpresa(id, empresaId);

        if (proceso.getEstado() == Proceso.EstadoProceso.PUBLICADO) {
            throw new IllegalArgumentException("El proceso ya está publicado");
        }
        gatewayService.validarSalidasProceso(id);

        proceso.setEstado(Proceso.EstadoProceso.PUBLICADO);
        Proceso guardado = procesoRepository.save(proceso);
        auditoriaService.registrar("Proceso", guardado.getId(), "PUBLICAR", "Publicado: " + guardado.getNombre());
        return guardado;
    }

    // 6. Inactivar: sale de los listados por defecto, pero se conserva
    public Proceso inactivarProceso(Long id, Long empresaId) {
        Proceso proceso = obtenerPorIdYEmpresa(id, empresaId);
        proceso.setEstado(Proceso.EstadoProceso.INACTIVO);
        Proceso guardado = procesoRepository.save(proceso);
        auditoriaService.registrar("Proceso", guardado.getId(), "INACTIVAR", "Inactivado: " + guardado.getNombre());
        return guardado;
    }

    // 7. Reactivar: vuelve a borrador para poder seguir editandolo
    public Proceso reactivarProceso(Long id, Long empresaId) {
        Proceso proceso = obtenerPorIdYEmpresa(id, empresaId);
        if (proceso.getEstado() != Proceso.EstadoProceso.INACTIVO) {
            throw new IllegalArgumentException("El proceso no está inactivo");
        }
        proceso.setEstado(Proceso.EstadoProceso.BORRADOR);
        Proceso guardado = procesoRepository.save(proceso);
        auditoriaService.registrar("Proceso", guardado.getId(), "REACTIVAR", "Reactivado: " + guardado.getNombre());
        return guardado;
    }

    // 8. Eliminar (borrado logico via @SQLDelete)
    public void eliminarProceso(Long id, Long empresaId) {
        Proceso proceso = obtenerPorIdYEmpresa(id, empresaId);
        auditoriaService.registrar("Proceso", id, "ELIMINAR", "Proceso eliminado");
        procesoRepository.delete(proceso);
    }

    private String normalizar(String valor, String mensajeSiFalta) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(mensajeSiFalta);
        }
        return valor.trim();
    }
}
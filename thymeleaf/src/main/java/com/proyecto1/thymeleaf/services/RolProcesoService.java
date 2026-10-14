package com.proyecto1.thymeleaf.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.proyecto1.thymeleaf.dto.ProcesoUsoDTO;
import com.proyecto1.thymeleaf.dto.RolProcesoRequestDTO;
import com.proyecto1.thymeleaf.dto.RolProcesoRespuestaDTO;
import com.proyecto1.thymeleaf.model.Empresa;
import com.proyecto1.thymeleaf.model.RolProceso;
import com.proyecto1.thymeleaf.repository.RolProcesoRepository;

/**
 * Logica de negocio de los roles de proceso (HU-17 crear, HU-18 editar,
 * HU-19 eliminar, HU-20 consultar).
 *
 * El rol pertenece a la empresa (no a un proceso) y las actividades se asignan
 * a funciones, no a personas. Todas las operaciones se hacen sobre la empresa
 * de la peticion: un rol de otra empresa "no existe" para quien lo pide.
 */
@Service
@Transactional
public class RolProcesoService {

    private final RolProcesoRepository rolProcesoRepository;
    private final EmpresaService empresaService;
    private final UsoRolProceso usoRolProceso;

    public RolProcesoService(RolProcesoRepository rolProcesoRepository, EmpresaService empresaService,
                             UsoRolProceso usoRolProceso) {
        this.rolProcesoRepository = rolProcesoRepository;
        this.empresaService = empresaService;
        this.usoRolProceso = usoRolProceso;
    }

    // 1. HU-20: catalogo de roles de la empresa, ordenado por nombre
    @Transactional(readOnly = true)
    public List<RolProcesoRespuestaDTO> listarPorEmpresa(Long empresaId) {
        return rolProcesoRepository.findByEmpresaId(empresaId)
                .stream()
                .map(rol -> toRespuesta(rol, empresaId))
                .toList();
    }

    // 2. HU-20: busqueda por nombre y paginacion
    @Transactional(readOnly = true)
    public Page<RolProcesoRespuestaDTO> buscar(Long empresaId, String nombre, Pageable pageable) {
        String filtro = (nombre == null || nombre.isBlank()) ? null : nombre.trim();
        return rolProcesoRepository.buscar(empresaId, filtro, pageable)
                .map(rol -> toRespuesta(rol, empresaId));
    }

    // 3. Obtener un rol verificando que pertenezca a la empresa
    @Transactional(readOnly = true)
    public RolProcesoRespuestaDTO obtenerPorIdYEmpresa(Long id, Long empresaId) {
        return toRespuesta(buscarRol(id, empresaId), empresaId);
    }

    // 4. HU-17: crear un rol en la empresa
    public RolProcesoRespuestaDTO crearRol(RolProcesoRequestDTO datos, Long empresaId) {
        Empresa empresa = empresaService.obtenerPorId(empresaId);

        String nombre = normalizar(datos.getNombre(), "El nombre del rol es obligatorio");
        String descripcion = normalizar(datos.getDescripcion(), "La descripcion del rol es obligatoria");
        if (rolProcesoRepository.existsByNombreAndEmpresaId(nombre, empresaId)) {
            throw new IllegalArgumentException("Ya existe un rol llamado '" + nombre + "' en la empresa");
        }

        RolProceso rol = new RolProceso();
        rol.setNombre(nombre);
        rol.setDescripcion(descripcion);
        rol.setEmpresa(empresa);

        return toRespuesta(rolProcesoRepository.save(rol), empresaId);
    }

    // 5. HU-18: editar nombre y descripcion. Las lanes guardan una referencia al
    // rol (no una copia del nombre), asi que el cambio se refleja en todas.
    public RolProcesoRespuestaDTO actualizarRol(Long id, RolProcesoRequestDTO datos, Long empresaId) {
        RolProceso rolExistente = buscarRol(id, empresaId);

        String nombre = normalizar(datos.getNombre(), "El nombre del rol es obligatorio");
        String descripcion = normalizar(datos.getDescripcion(), "La descripcion del rol es obligatoria");
        if (rolProcesoRepository.existsByNombreAndEmpresaIdAndIdNot(nombre, empresaId, id)) {
            throw new IllegalArgumentException("Ya existe un rol llamado '" + nombre + "' en la empresa");
        }

        rolExistente.setNombre(nombre);
        rolExistente.setDescripcion(descripcion);

        return toRespuesta(rolProcesoRepository.save(rolExistente), empresaId);
    }

    // 6. HU-19: eliminar (borrado logico via @SQLDelete) solo si el rol no esta en uso
    public void eliminarRol(Long id, Long empresaId) {
        RolProceso rol = buscarRol(id, empresaId);

        List<ProcesoUsoDTO> usos = usoRolProceso.procesosDondeSeUsa(id, empresaId);
        if (!usos.isEmpty()) {
            String procesos = usos.stream().map(ProcesoUsoDTO::nombre).collect(Collectors.joining(", "));
            throw new IllegalArgumentException("No se puede eliminar el rol '" + rol.getNombre()
                    + "' porque esta en uso en los procesos: " + procesos);
        }

        rolProcesoRepository.delete(rol);
    }

    private RolProceso buscarRol(Long id, Long empresaId) {
        return rolProcesoRepository.findByIdAndEmpresaId(id, empresaId)
                .orElseThrow(() -> new IllegalArgumentException("El rol no existe o no pertenece a su empresa"));
    }

    private RolProcesoRespuestaDTO toRespuesta(RolProceso rol, Long empresaId) {
        return RolProcesoRespuestaDTO.desde(rol, usoRolProceso.procesosDondeSeUsa(rol.getId(), empresaId));
    }

    private String normalizar(String valor, String mensajeSiFalta) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(mensajeSiFalta);
        }
        return valor.trim();
    }
}

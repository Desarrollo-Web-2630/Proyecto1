package com.proyecto1.thymeleaf.services;

import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.proyecto1.thymeleaf.dto.PermisoPoolDTO;
import com.proyecto1.thymeleaf.model.PermisoPool;
import com.proyecto1.thymeleaf.model.Pool;
import com.proyecto1.thymeleaf.model.Usuario;
import com.proyecto1.thymeleaf.repository.PermisoPoolRepository;
import com.proyecto1.thymeleaf.util.EmpresaActual;

@Service
@Transactional
public class PermisoPoolService {

    private final PermisoPoolRepository permisoRepository;
    private final PoolService poolService;
    private final AuditoriaService auditoriaService;

    public PermisoPoolService(PermisoPoolRepository permisoRepository, PoolService poolService,
                              AuditoriaService auditoriaService) {
        this.permisoRepository = permisoRepository;
        this.poolService = poolService;
        this.auditoriaService = auditoriaService;
    }

    @Transactional(readOnly = true)
    public List<PermisoPoolDTO> listar(Long poolId, Long procesoId, Long empresaId) {
        poolService.obtenerPorIdYEmpresa(poolId, procesoId, empresaId);
        return permisoRepository.findByPoolId(poolId).stream().map(PermisoPoolDTO::desde).toList();
    }

    public List<PermisoPoolDTO> guardar(Long poolId, Long procesoId, PermisoPoolDTO datos, Long empresaId) {
        Pool pool = poolService.obtenerPorIdYEmpresa(poolId, procesoId, empresaId);
        if (!datos.esRolValido()) {
            throw new IllegalArgumentException("El rol de acceso no es válido");
        }
        PermisoPool permiso = permisoRepository.findByPoolIdAndRolAcceso(poolId, datos.getRolAcceso())
                .orElseGet(() -> new PermisoPool(null, pool, datos.getRolAcceso(), false, false, false));
        permiso.setPuedeCrear(datos.isPuedeCrear());
        permiso.setPuedeEditar(datos.isPuedeEditar());
        permiso.setPuedeEliminar(datos.isPuedeEliminar());
        permisoRepository.save(permiso);

        auditoriaService.registrar("Pool", poolId, "PERMISOS",
                datos.getRolAcceso() + " crear=" + datos.isPuedeCrear()
                        + " editar=" + datos.isPuedeEditar() + " eliminar=" + datos.isPuedeEliminar());
        return listar(poolId, procesoId, empresaId);
    }

    public void verificar(Pool pool, String accion) {
        Usuario.RolAcceso rol = EmpresaActual.usuario().getRolAcceso();
        if (rol == Usuario.RolAcceso.ADMIN) {
            return;
        }
        boolean permitido = permisoRepository.findByPoolIdAndRolAcceso(pool.getId(), rol.name())
                .map(permiso -> switch (accion) {
                    case "CREAR" -> permiso.isPuedeCrear();
                    case "EDITAR" -> permiso.isPuedeEditar();
                    case "ELIMINAR" -> permiso.isPuedeEliminar();
                    default -> false;
                })
                .orElseGet(() -> rol == Usuario.RolAcceso.EDITOR);
        if (!permitido) {
            throw new AccessDeniedException("Su rol no puede " + accion.toLowerCase() + " en este pool");
        }
    }
}

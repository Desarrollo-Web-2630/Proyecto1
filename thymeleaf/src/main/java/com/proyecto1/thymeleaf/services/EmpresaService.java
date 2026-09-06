package com.proyecto1.thymeleaf.services;

import com.proyecto1.thymeleaf.model.Empresa;
import com.proyecto1.thymeleaf.repository.EmpresaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Logica de negocio de las empresas (HU-01 Registro de empresa).
 *
 * La empresa es la raiz del aislamiento multi-tenant: no recibe empresaId
 * porque ella misma es el tenant.
 */
@Service
@Transactional
public class EmpresaService {

    private final EmpresaRepository empresaRepository;

    public EmpresaService(EmpresaRepository empresaRepository) {
        this.empresaRepository = empresaRepository;
    }

    // 1. Listar todas las empresas registradas
    @Transactional(readOnly = true)
    public List<Empresa> listarTodas() {
        return empresaRepository.findAll();
    }

    // 2. Obtener una empresa por su id
    @Transactional(readOnly = true)
    public Empresa obtenerPorId(Long id) {
        return empresaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("La empresa no existe"));
    }

    // 3. Registrar una empresa nueva
    public Empresa registrarEmpresa(Empresa empresa) {
        validarDatosObligatorios(empresa);

        String nit = empresa.getNit().trim();
        if (empresaRepository.existsByNit(nit)) {
            throw new IllegalArgumentException("Ya existe una empresa registrada con el NIT '" + nit + "'");
        }

        empresa.setNombre(empresa.getNombre().trim());
        empresa.setNit(nit);
        empresa.setCorreo(empresa.getCorreo().trim());

        return empresaRepository.save(empresa);
    }

    // 4. Actualizar los datos de una empresa
    public Empresa actualizarEmpresa(Long id, Empresa empresaDetalles) {
        Empresa empresaExistente = obtenerPorId(id);
        validarDatosObligatorios(empresaDetalles);

        String nit = empresaDetalles.getNit().trim();
        // Solo se valida el NIT contra otras empresas si realmente cambio
        if (!nit.equals(empresaExistente.getNit()) && empresaRepository.existsByNit(nit)) {
            throw new IllegalArgumentException("Ya existe una empresa registrada con el NIT '" + nit + "'");
        }

        empresaExistente.setNombre(empresaDetalles.getNombre().trim());
        empresaExistente.setNit(nit);
        empresaExistente.setCorreo(empresaDetalles.getCorreo().trim());

        return empresaRepository.save(empresaExistente);
    }

    // 5. Eliminar (borrado logico via @SQLDelete)
    public void eliminarEmpresa(Long id) {
        Empresa empresa = obtenerPorId(id);
        empresaRepository.delete(empresa);
    }

    private void validarDatosObligatorios(Empresa empresa) {
        if (empresa.getNombre() == null || empresa.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre de la empresa es obligatorio");
        }
        if (empresa.getNit() == null || empresa.getNit().isBlank()) {
            throw new IllegalArgumentException("El NIT de la empresa es obligatorio");
        }
        if (empresa.getCorreo() == null || empresa.getCorreo().isBlank()) {
            throw new IllegalArgumentException("El correo de la empresa es obligatorio");
        }
    }
}

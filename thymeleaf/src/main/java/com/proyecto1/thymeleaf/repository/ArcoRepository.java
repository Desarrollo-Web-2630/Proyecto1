package com.proyecto1.thymeleaf.repository;

import com.proyecto1.thymeleaf.model.Arco;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ArcoRepository extends JpaRepository<Arco, Long> {

    List<Arco> findByProcesoId(Long procesoId);

    Optional<Arco> findByIdAndProcesoEmpresaId(Long id, Long empresaId);

    boolean existsByOrigenIdAndDestinoId(Long origenId, Long destinoId);

    List<Arco> findByOrigenId(Long origenId);

    List<Arco> findByDestinoId(Long destinoId);
}

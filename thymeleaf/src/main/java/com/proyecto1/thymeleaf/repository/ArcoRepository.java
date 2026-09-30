package com.proyecto1.thymeleaf.repository;

import com.proyecto1.thymeleaf.model.Arco;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
 
import java.util.List;
import java.util.Optional;

public interface ArcoRepository extends JpaRepository<Arco, Long> {

@Query("SELECT a FROM Arco a WHERE a.proceso.id = :procesoId AND a.status = 0")
List<Arco> findByProcesoId(@Param("procesoId") Long procesoId);

@Query("SELECT a FROM Arco a WHERE a.id = :id " +
        "AND a.proceso.empresa.id = :empresaId AND a.status = 0")
Optional<Arco> findByIdAndProcesoEmpresaId(@Param("id") Long id, @Param("empresaId") Long empresaId);

@Query("SELECT a FROM Arco a WHERE a.origen.id = :elementoId AND a.status = 0")
List<Arco> findByOrigenId(@Param("elementoId") Long elementoId);

@Query("SELECT a FROM Arco a WHERE a.destino.id = :elementoId AND a.status = 0")
List<Arco> findByDestinoId(@Param("elementoId") Long elementoId);
}
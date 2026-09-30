package com.proyecto1.thymeleaf.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.proyecto1.thymeleaf.model.Actividad;


public interface ActividadRepository extends JpaRepository<Actividad, Long> {

@Query("SELECT a FROM Actividad a WHERE a.proceso.id = :procesoId AND a.status = 0")
List<Actividad> findByProcesoId(@Param("procesoId") Long procesoId);

@Query("SELECT a FROM Actividad a WHERE a.proceso.id = :procesoId " +
        "AND a.laneId = :laneId AND a.status = 0")
List<Actividad> findByProcesoIdAndLaneId(@Param("procesoId") Long procesoId,
                                            @Param("laneId") Long laneId);

// Aislamiento por empresa: se llega a la empresa navegando
// actividad -> proceso -> empresa, nunca confiando en un id suelto.
@Query("SELECT a FROM Actividad a WHERE a.id = :id " +
        "AND a.proceso.empresa.id = :empresaId AND a.status = 0")
Optional<Actividad> findByIdAndProcesoEmpresaId(@Param("id") Long id,
                                                    @Param("empresaId") Long empresaId);

@Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END FROM Actividad a " +
        "WHERE LOWER(a.nombre) = LOWER(:nombre) AND a.proceso.id = :procesoId AND a.status = 0")
boolean existsByNombreAndProcesoId(@Param("nombre") String nombre, @Param("procesoId") Long procesoId);

@Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END FROM Actividad a " +
        "WHERE LOWER(a.nombre) = LOWER(:nombre) AND a.proceso.id = :procesoId " +
        "AND a.id <> :idActual AND a.status = 0")
boolean existsByNombreAndProcesoIdAndIdNot(@Param("nombre") String nombre,
                                            @Param("procesoId") Long procesoId,
                                            @Param("idActual") Long idActual);
}

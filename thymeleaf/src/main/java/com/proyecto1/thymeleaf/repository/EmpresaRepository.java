package com.proyecto1.thymeleaf.repository;

import com.proyecto1.thymeleaf.model.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface EmpresaRepository extends JpaRepository<Empresa, Long> {

    @Query("SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END " +
           "FROM Empresa e WHERE e.nit = :nit AND e.status = 0")
    boolean existsByNit(@Param("nit") String nit);
}

package com.proyecto1.thymeleaf.repository;

import com.proyecto1.thymeleaf.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    @Query("SELECT u FROM Usuario u WHERE LOWER(u.correo) = LOWER(:correo) AND u.status = 0")
    Optional<Usuario> findByCorreoIgnoreCase(@Param("correo") String correo);

    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END " +
        "FROM Usuario u WHERE LOWER(u.correo) = LOWER(:correo) AND u.status = 0")
    boolean existsByCorreoIgnoreCase(@Param("correo") String correo);

    @Query("SELECT u FROM Usuario u WHERE u.empresa.id = :empresaId AND u.status = 0")
    List<Usuario> findByEmpresaId(@Param("empresaId") Long empresaId);

    @Query("SELECT u FROM Usuario u " +
        "WHERE u.id = :id AND u.empresa.id = :empresaId AND u.status = 0")
    Optional<Usuario> findByIdAndEmpresaId(@Param("id") Long id, @Param("empresaId") Long empresaId);
}

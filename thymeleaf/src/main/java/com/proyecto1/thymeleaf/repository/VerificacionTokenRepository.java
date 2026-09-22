package com.proyecto1.thymeleaf.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.proyecto1.thymeleaf.model.VerificacionToken;

public interface VerificacionTokenRepository extends JpaRepository<VerificacionToken, Long> {

    Optional<VerificacionToken> findByToken(String token);

    void deleteByToken(String token);

    long countByUsuarioIdAndExpiracionAfter(Long usuarioId, java.time.Instant instante);
}

package com.proyecto1.thymeleaf.repository;

import com.proyecto1.thymeleaf.model.VerificacionToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VerificacionTokenRepository extends JpaRepository<VerificacionToken, Long> {

    Optional<VerificacionToken> findByToken(String token);

    void deleteByToken(String token);
}

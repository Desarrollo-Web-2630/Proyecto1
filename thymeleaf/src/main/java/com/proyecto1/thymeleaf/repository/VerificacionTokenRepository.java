package com.proyecto1.thymeleaf.repository;

import com.proyecto1.thymeleaf.model.VerificacionToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VerificacionTokenRepository extends JpaRepository<VerificacionToken, Long> {

    Optional<VerificacionToken> findByToken(String token);

    void deleteByToken(String token);

    // Cuantos tokens de este usuario expiran despues de un instante dado. Como
    // todos se crean con la misma vigencia, sirve para saber cuantos se
    // generaron en la ultima hora sin agregar una columna de fecha.
    long countByUsuarioIdAndExpiracionAfter(Long usuarioId, java.time.Instant instante);
}

package com.proyecto1.thymeleaf.repository;

import java.time.Instant;

import org.springframework.data.jpa.repository.JpaRepository;

import com.proyecto1.thymeleaf.model.TokenRevocado;

public interface TokenRevocadoRepository extends JpaRepository<TokenRevocado, String> {

    void deleteByExpiracionBefore(Instant fecha);
}

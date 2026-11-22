package com.proyecto1.thymeleaf.model;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "token_revocado")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TokenRevocado {

    @Id
    @Column(nullable = false, length = 64)
    private String jti;

    @Column(nullable = false)
    private Instant expiracion;
}

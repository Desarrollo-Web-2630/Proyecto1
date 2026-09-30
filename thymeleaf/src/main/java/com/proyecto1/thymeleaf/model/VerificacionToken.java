package com.proyecto1.thymeleaf.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "verificacion_token")
@Getter
@Setter
@NoArgsConstructor
public class VerificacionToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(nullable = false)
    private Instant expiracion;

    @Column(nullable = false)
    private boolean usado = false;

    public VerificacionToken(String token, Usuario usuario, Instant expiracion) {
        this.token = token;
        this.usuario = usuario;
        this.expiracion = expiracion;
    }
}

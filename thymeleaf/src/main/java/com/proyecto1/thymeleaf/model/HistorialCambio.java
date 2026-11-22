package com.proyecto1.thymeleaf.model;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "historial_cambio")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HistorialCambio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long empresaId;

    @Column(nullable = false)
    private String entidad;

    @Column(nullable = false)
    private Long entidadId;

    @Column(nullable = false)
    private String accion;

    @Column(length = 1000)
    private String descripcion;

    @Column(nullable = false)
    private String usuarioCorreo;

    @Column(nullable = false)
    private Instant fecha;
}

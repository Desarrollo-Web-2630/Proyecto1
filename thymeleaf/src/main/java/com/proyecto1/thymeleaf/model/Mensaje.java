package com.proyecto1.thymeleaf.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

// HU-25 a HU-28: un mensaje documenta la comunicacion entre dos pools distintos
// del mismo proceso; claveCorrelacion indica a que caso corresponde el mensaje.
// El sistema solo modela el envio, no lo ejecuta.

@Entity
@Table(name = "mensaje")
@Getter
@Setter
@NoArgsConstructor
@SQLRestriction("status = 0")
@SQLDelete(sql = "UPDATE mensaje SET status = 1 WHERE id = ?")
public class Mensaje {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String claveCorrelacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proceso_id", nullable = false)
    private Proceso proceso;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pool_origen_id", nullable = false)
    private Pool poolOrigen;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pool_destino_id", nullable = false)
    private Pool poolDestino;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "elemento_origen_id")
    private ElementoConectable elementoOrigen;

    @Column(nullable = false)
    private Integer status = 0;
}

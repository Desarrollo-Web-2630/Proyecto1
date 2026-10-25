package com.proyecto1.thymeleaf.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

// HU-21: un pool representa un participante del proceso (la empresa propietaria,
// un cliente, un proveedor o un sistema externo).

@Entity
@Table(name = "pool")
@Getter
@Setter
@NoArgsConstructor
@SQLRestriction("status = 0")
@SQLDelete(sql = "UPDATE pool SET status = 1 WHERE id = ?")
public class Pool {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoPool tipo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proceso_id", nullable = false)
    private Proceso proceso;

    @Column(nullable = false)
    private Integer status = 0;

    public enum TipoPool { PROPIETARIO, CLIENTE, PROVEEDOR, SISTEMA_EXTERNO }
}

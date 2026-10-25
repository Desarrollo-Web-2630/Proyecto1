package com.proyecto1.thymeleaf.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

// HU-22: una lane es una division interna de un pool, asociada a un rol de
// proceso responsable de las actividades que agrupa.

@Entity
@Table(name = "lane")
@Getter
@Setter
@NoArgsConstructor
@SQLRestriction("status = 0")
@SQLDelete(sql = "UPDATE lane SET status = 1 WHERE id = ?")
public class Lane {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private Integer orden = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pool_id", nullable = false)
    private Pool pool;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rol_proceso_id", nullable = false)
    private RolProceso rolProceso;

    @Column(nullable = false)
    private Integer status = 0;
}

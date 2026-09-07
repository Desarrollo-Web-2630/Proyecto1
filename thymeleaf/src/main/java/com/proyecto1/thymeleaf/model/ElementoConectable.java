package com.proyecto1.thymeleaf.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "elemento_conectable")
@Inheritance(strategy = InheritanceType.JOINED)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SQLRestriction("status = 0")
@SQLDelete(sql = "UPDATE elemento_conectable SET status = 1 WHERE id = ?")

// Esta clase es para representar un elemento que puede ser conectado a otros elementos en un proceso. Puede ser una actividad, un gateway o un arco.

public abstract class ElementoConectable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private Integer posicionX;

    @Column(nullable = false)
    private Integer posicionY;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proceso_id", nullable = false)
    private Proceso proceso;

    @Column(nullable = false)
    private Integer status = 0;
}
package com.proyecto1.thymeleaf.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "actividad", uniqueConstraints = @UniqueConstraint(columnNames = {"nombre", "proceso_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SQLRestriction("status = 0")
@SQLDelete(sql = "UPDATE actividad SET status = 1 WHERE id = ?")
public class Actividad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String tipoActividad;

    @Column(nullable = false)
    private Integer posicionX;

    @Column(nullable = false)
    private Integer posicionY;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proceso_id", nullable = false)
    private Proceso proceso;

    @Column(name = "lane_id", nullable = false)
    private Long laneId;

    @Column(nullable = false)
    private Integer status = 0;
}

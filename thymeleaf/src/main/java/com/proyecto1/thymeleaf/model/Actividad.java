package com.proyecto1.thymeleaf.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "actividad")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
@SQLRestriction("status = 0")
public class Actividad extends ElementoConectable {

    @Column(nullable = false)
    private String tipoActividad;

    @Column(name = "lane_id", nullable = false)
    private Long laneId;
}

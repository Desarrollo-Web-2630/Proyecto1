package com.proyecto1.thymeleaf.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Table(name = "actividad")
@Getter @Setter @NoArgsConstructor
@DiscriminatorValue("ACTIVIDAD")
public class Actividad extends ElementoConectable {

    @Column(nullable = false)
    private String tipoActividad;

    @Column(name = "lane_id", nullable = false)
    private Long laneId;
}
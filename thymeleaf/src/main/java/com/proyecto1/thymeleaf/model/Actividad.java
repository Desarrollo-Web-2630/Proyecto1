package com.proyecto1.thymeleaf.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;


@Entity
@Table(name = "actividad")
@Getter
@Setter
@SQLRestriction("status = 0")
@SQLDelete(sql = "UPDATE actividad SET status = 1 WHERE id = ?")
public class Actividad extends ElementoConectable {

    @Column(nullable = false)
    private String tipoActividad;

    @Column(name = "lane_id", nullable = false)
    private Long laneId;
}

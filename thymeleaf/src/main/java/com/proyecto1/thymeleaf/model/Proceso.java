package com.proyecto1.thymeleaf.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "proceso", uniqueConstraints = @UniqueConstraint(columnNames = {"nombre", "empresa_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SQLRestriction("status = 0")
@SQLDelete(sql = "UPDATE proceso SET status = 1 WHERE id = ?")
public class Proceso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String descripcion;

    @Column(nullable = false)
    private String categoria;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoProceso estado = EstadoProceso.BORRADOR;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @OneToMany(mappedBy = "proceso", fetch = FetchType.LAZY)
    private List<Actividad> actividades = new ArrayList<>();

    @Column(nullable = false)
    private Integer status = 0;

    public enum EstadoProceso {
        BORRADOR,
        PUBLICADO
    }
}

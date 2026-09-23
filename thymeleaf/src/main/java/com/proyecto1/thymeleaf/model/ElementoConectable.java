package com.proyecto1.thymeleaf.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import com.fasterxml.jackson.annotation.JsonIgnore;


@Entity
@Table(name = "elemento_conectable")
@Inheritance(strategy = InheritanceType.JOINED)
@Getter
@Setter
@SQLRestriction("status = 0")
@SQLDelete(sql = "UPDATE elemento_conectable SET status = 1 WHERE id = ?")
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
    
    @JsonIgnore
    private Proceso proceso;
 
    @Column(nullable = false)
    private Integer status = 0;

    @OneToMany(mappedBy = "origen", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Arco> arcosSalientes = new ArrayList<>();

    @OneToMany(mappedBy = "destino", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Arco> arcosEntrantes = new ArrayList<>();
}
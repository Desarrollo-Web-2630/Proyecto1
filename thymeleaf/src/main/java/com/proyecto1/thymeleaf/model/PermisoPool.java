package com.proyecto1.thymeleaf.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "permiso_pool", uniqueConstraints = @UniqueConstraint(
        columnNames = {"pool_id", "rol_acceso"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PermisoPool {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pool_id", nullable = false)
    private Pool pool;

    @Column(name = "rol_acceso", nullable = false)
    private String rolAcceso;

    @Column(nullable = false)
    private boolean puedeCrear;

    @Column(nullable = false)
    private boolean puedeEditar;

    @Column(nullable = false)
    private boolean puedeEliminar;
}

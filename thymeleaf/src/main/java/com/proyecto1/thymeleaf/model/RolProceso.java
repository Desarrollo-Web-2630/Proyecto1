package com.proyecto1.thymeleaf.model;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Rol de proceso (HU-17 a HU-20): la funcion responsable de una actividad
 * (Analista, Supervisor, Auditor...), NO la persona. En el diagrama se dibuja
 * como lane.
 *
 * El rol pertenece a la EMPRESA y no a un proceso, de modo que puede usarse en
 * varios procesos. No se confunde con Usuario.rolAcceso (admin/editor/lectura),
 * que dice que puede hacer un usuario en el sistema.
 *
 * El nombre es unico por empresa, pero esa regla se valida en el servicio y no
 * con un UniqueConstraint: el borrado es logico y una restriccion en la base
 * impediria volver a crear un rol con el nombre de uno eliminado.
 */
@Entity
@Table(name = "rol_proceso")
@Getter
@Setter
@NoArgsConstructor
@SQLRestriction("status = 0")
@SQLDelete(sql = "UPDATE rol_proceso SET status = 1 WHERE id = ?")
public class RolProceso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, length = 500)
    private String descripcion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    // 0 = activo, 1 = eliminado (borrado logico, nada se borra fisicamente)
    @Column(nullable = false)
    private Integer status = 0;
}

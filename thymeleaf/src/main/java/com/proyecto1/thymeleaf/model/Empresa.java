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
@Table(name = "empresa")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SQLRestriction("status = 0")
@SQLDelete(sql = "UPDATE empresa SET status = 1 WHERE id = ?")
public class Empresa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false, unique = true)
    private String nit;

    @Column(nullable = false)
    private String correo;

    @Column(nullable = false)
    private Integer status = 0;

    @OneToMany(mappedBy = "empresa", fetch = FetchType.LAZY)
    private List<Usuario> usuarios = new ArrayList<>();
}

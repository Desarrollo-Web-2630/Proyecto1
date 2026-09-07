package com.proyecto1.thymeleaf.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "gateways")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLRestriction("status = 0")
@SQLDelete(sql = "UPDATE gateways SET status = 1 WHERE id = ?")
public class Gateway {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;

    @Enumerated(EnumType.STRING)
    private TipoGateway tipo; // <--- AQUÍ SE USA EL ENUM

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proceso_id", nullable = false)
    private Proceso proceso;

    @Column(nullable = false)
    @Builder.Default
    private Integer status = 0;

    // DEFINICIÓN DEL ENUM DENTRO DE LA CLASE GATEWAY
    public enum TipoGateway {
        EXCLUSIVO,
        PARALELO,
        INCLUSIVO
    }
}
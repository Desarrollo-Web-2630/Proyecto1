package com.proyecto1.thymeleaf.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "gateway")
@Getter
@Setter
@SQLRestriction("status = 0")
@SQLDelete(sql = "UPDATE gateway SET status = 1 WHERE id = ?")

public class Gateway extends ElementoConectable {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoGateway tipo;
 
    public enum TipoGateway {
        EXCLUSIVO,
        PARALELO,
        INCLUSIVO
    }
}
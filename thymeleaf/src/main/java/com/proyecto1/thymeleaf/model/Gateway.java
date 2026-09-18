package com.proyecto1.thymeleaf.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "gateways")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
@SQLRestriction("status = 0")
public class Gateway extends ElementoConectable {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoGateway tipo;

    public boolean requiereCondicionEnSalidas() {
        return tipo == TipoGateway.EXCLUSIVO || tipo == TipoGateway.INCLUSIVO;
    }

    public enum TipoGateway {
        EXCLUSIVO,
        PARALELO,
        INCLUSIVO
    }
}

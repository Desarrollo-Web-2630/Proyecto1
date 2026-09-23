package com.proyecto1.thymeleaf.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "gateways")
@Getter @Setter @NoArgsConstructor
@DiscriminatorValue("GATEWAY")
public class Gateway extends ElementoConectable {

    @Enumerated(EnumType.STRING)
    private TipoGateway tipo;

    public enum TipoGateway { EXCLUSIVO, PARALELO, INCLUSIVO }
}
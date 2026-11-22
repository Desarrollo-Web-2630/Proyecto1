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
@Table(name = "proceso_compartido", uniqueConstraints = @UniqueConstraint(
        columnNames = {"proceso_id", "empresa_invitada_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProcesoCompartido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proceso_id", nullable = false)
    private Proceso proceso;

    @Column(name = "empresa_invitada_id", nullable = false)
    private Long empresaInvitadaId;

    @Column(nullable = false)
    private boolean activo = true;
}

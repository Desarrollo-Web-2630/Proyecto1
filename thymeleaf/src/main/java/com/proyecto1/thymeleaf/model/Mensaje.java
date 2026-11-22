package com.proyecto1.thymeleaf.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "mensaje")
@Getter
@Setter
@NoArgsConstructor
@SQLRestriction("status = 0")
@SQLDelete(sql = "UPDATE mensaje SET status = 1 WHERE id = ?")
public class Mensaje {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String claveCorrelacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proceso_id", nullable = false)
    private Proceso proceso;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pool_origen_id", nullable = false)
    private Pool poolOrigen;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pool_destino_id", nullable = false)
    private Pool poolDestino;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "elemento_origen_id")
    private ElementoConectable elementoOrigen;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoMensaje tipoMensaje = TipoMensaje.THROW;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private VarianteMensaje variante = VarianteMensaje.INICIO;

    @Column(length = 255)
    private String destinoExterno;

    @Column(length = 4000)
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private PoliticaFallo politicaFallo;

    @Column(nullable = false)
    private Integer status = 0;

    public enum TipoMensaje { THROW, CATCH }

    public enum VarianteMensaje { INICIO, INTERMEDIO }

    public enum PoliticaFallo { REINTENTAR, FALLAR, REGISTRAR }
}

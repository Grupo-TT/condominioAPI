package com.condominio.persistence.model;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@Entity
public class Obligacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private LocalDate fechaGenerada;
    @Column
    private int monto;

    private LocalDate fechaLimite;
    private int tasaInteres;
    private int interes;
    @Column(columnDefinition = "int default 0")
    private int montoPagado;
    private String motivo;
    private String titulo;

    private int valorTotal;
    private int valorPendiente;

    @ManyToOne(targetEntity = Casa.class)
    @JoinColumn(nullable = false)
    private Casa casa;

    @Enumerated(EnumType.STRING)
    private TipoPago tipoPago;

    @Enumerated(EnumType.STRING)
    private TipoObligacion tipoObligacion;

    @Enumerated(EnumType.STRING)
    private EstadoPago estadoPago;

    @PrePersist
    @PreUpdate
    private void calcularValores() {
        this.valorTotal = this.monto + this.interes;
        this.valorPendiente = this.valorTotal - this.montoPagado;
    }
}

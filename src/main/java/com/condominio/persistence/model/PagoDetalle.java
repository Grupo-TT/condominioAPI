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
public class PagoDetalle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private int montoPagado;

    @ManyToOne(targetEntity = Obligacion.class)
    @JoinColumn(nullable = false)
    private Obligacion obligacion;

    @ManyToOne(targetEntity = Pago.class)
    @JoinColumn(nullable = false)
    private Pago pago;
}

package com.condominio.persistence.model;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.Date;

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

    private Date fechaLimite;
    private int diasGracias;
    private int diasMaxMora;
    private int tasaInteres;
    private int interes;
    private String motivo;

    @ManyToOne(targetEntity = Casa.class)
    @JoinColumn(nullable = false)
    private Casa casa;

    @Enumerated(EnumType.STRING)
    private TipoPago tipoPago;

    @Enumerated(EnumType.STRING)
    private EstadoPago estadoPago;
}

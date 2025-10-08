package com.condominio.persistence.model;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@Entity
public class Reserva {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private boolean estado;

    @ManyToOne(targetEntity = SolicitudReservaRecurso.class)
    @JoinColumn(nullable = false)
    private SolicitudReservaRecurso solicitudReservaRecurso;
}

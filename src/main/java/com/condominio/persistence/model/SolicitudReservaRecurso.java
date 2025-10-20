package com.condominio.persistence.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@Service
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class SolicitudReservaRecurso {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private LocalDate fechaSolicitud;

    @Column
    private LocalTime horaInicio;
    @Column
    private LocalTime horaFin;
    @Column
    private int numeroInvitados;

    @Enumerated(EnumType.STRING)
    private EstadoSolicitud estadoSolicitud;

    @ManyToOne(targetEntity = Casa.class)
    @JoinColumn(nullable = false)
    private Casa casa;

    @ManyToOne(targetEntity = RecursoComun.class)
    @JoinColumn(nullable = false)
    private RecursoComun recursoComun;
}

package com.condominio.dto.response;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SolicitudReservaUpdateDTO {

    private Long id;

    private LocalDate fechaSolicitud;

    private LocalTime horaInicio;

    private LocalTime horaFin;

    private int numeroInvitados;
}

package com.condominio.dto.request;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SolicitudReservaUpdateDTO {
    private Long idSolicitud;
    private LocalDate fechaSolicitud;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private int numeroInvitados;
}

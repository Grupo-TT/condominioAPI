package com.condominio.dto.response;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SolicitudRecursoPropiDTO {
    private Long idRecurso;
    private Long idSolicitante;
    private LocalDate fechaSolicitud;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private int numeroInvitados;
}

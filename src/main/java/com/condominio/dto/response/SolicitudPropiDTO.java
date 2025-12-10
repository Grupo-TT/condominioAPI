package com.condominio.dto.response;

import com.condominio.persistence.model.EstadoSolicitud;
import com.condominio.persistence.model.RecursoComun;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SolicitudPropiDTO {
    private Long id;
    private EstadoSolicitud estado;
    private LocalDate fechaSolicitud;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private int numeroInvitados;
    private RecursoComun recursoComun;
}

package com.condominio.dto.response;

import com.condominio.persistence.model.Casa;
import com.condominio.persistence.model.EstadoSolicitud;
import com.condominio.persistence.model.RecursoComun;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SolicitudReservaRecursoDTO {


    private LocalDate fechaSolicitud;

    private LocalTime horaInicio;

    private LocalTime horaFin;

    private int numeroInvitados;

    private EstadoSolicitud estadoSolicitud;

    private Casa casa;

    private PersonaSimpleDTO solicitante;

    private RecursoComun recursoComun;
}

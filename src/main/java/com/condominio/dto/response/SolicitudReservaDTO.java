package com.condominio.dto.response;

import com.condominio.persistence.model.EstadoSolicitud;
import com.condominio.persistence.model.TipoRecursoComun;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SolicitudReservaDTO {
    private Long id;
    private LocalDate fechaCreacion;
    private LocalDate fechaReserva;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private int numeroInvitados;
    private EstadoSolicitud estadoSolicitud;
    private Long idRecurso;
    private String nombre;
    private String descripcion;
    private TipoRecursoComun tipoRecursoComun;
}

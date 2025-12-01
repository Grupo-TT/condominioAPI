package com.condominio.dto.response;

import com.condominio.persistence.model.Casa;
import com.condominio.persistence.model.EstadoSolicitud;
import com.condominio.persistence.model.TipoObra;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SolicitudReparacionLocativaDTO {

    private Long id;

    private LocalDate fechaRealizacion;

    private String motivo;

    private String responsable;

    private EstadoSolicitud estadoSolicitud;

    private LocalDate inicioObra;

    private LocalDate finObra;

    private Casa casa;

    private PersonaSimpleDTO solicitante;

    private TipoObra tipoObra;

    private String tipoObraDetalle;

    private String comentarios;
}

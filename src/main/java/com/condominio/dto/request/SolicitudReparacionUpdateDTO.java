package com.condominio.dto.request;

import com.condominio.persistence.model.EstadoSolicitud;
import com.condominio.persistence.model.TipoObra;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SolicitudReparacionUpdateDTO {
    private Long id;

    private LocalDate fechaRealizacion;

    private String motivo;

    private String responsable;

    private LocalDate inicioObra;

    private LocalDate finObra;

    private EstadoSolicitud estadoSolicitud;

    private TipoObra tipoObra;

    private String tipoObraDetalle;

    private String comentarios;
}

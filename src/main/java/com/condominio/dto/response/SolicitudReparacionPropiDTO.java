package com.condominio.dto.response;

import com.condominio.persistence.model.TipoObra;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SolicitudReparacionPropiDTO {
    private LocalDate fechaRealizacion;
    private String motivo;
    private String responsable;
    private LocalDate inicioObra;
    private LocalDate finObra;
    private TipoObra tipoObra;
    private String tipoObraDetalle;
}

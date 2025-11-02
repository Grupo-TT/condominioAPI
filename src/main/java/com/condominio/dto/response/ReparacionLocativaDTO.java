package com.condominio.dto.response;

import com.condominio.persistence.model.Casa;
import com.condominio.persistence.model.EstadoSolicitud;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReparacionLocativaDTO {

    private Long id;

    private SolicitudReparacionLocativaDTO solicitudReparacionLocativa;


}

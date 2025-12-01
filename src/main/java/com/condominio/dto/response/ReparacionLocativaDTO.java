package com.condominio.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReparacionLocativaDTO {

    private Long id;

    private SolicitudReparacionLocativaDTO solicitudReparacionLocativa;


}

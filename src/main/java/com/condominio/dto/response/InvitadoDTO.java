package com.condominio.dto.response;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvitadoDTO {
    private Long idSolicitud;
    private int cantidadInvitados;
}

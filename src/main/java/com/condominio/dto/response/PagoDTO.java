package com.condominio.dto.response;


import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PagoDTO {
    private long idObligacion;
    private int montoAPagar;
    private String soporte;
}

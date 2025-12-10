package com.condominio.dto.response;


import com.condominio.persistence.model.TipoObligacion;
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
    private TipoObligacion tipoObligacion;
}

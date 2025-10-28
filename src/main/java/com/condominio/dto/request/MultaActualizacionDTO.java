package com.condominio.dto.request;

import com.condominio.persistence.model.TipoPago;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MultaActualizacionDTO {

    private Long idCasa;
    private int monto;
    private String titulo;
    private String motivo;
    private TipoPago tipoPago;
}

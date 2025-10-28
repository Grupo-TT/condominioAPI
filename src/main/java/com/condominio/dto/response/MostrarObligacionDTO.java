package com.condominio.dto.response;

import com.condominio.persistence.model.EstadoPago;
import com.condominio.persistence.model.TipoObligacion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MostrarObligacionDTO {

    private Long id;
    private String estado;
    private String titulo;
    private String motivo;
    private int casa;
    private int monto;
    private int valorTotal;
    private int saldoPendiente;
    private TipoObligacion tipoObligacion;
    private EstadoPago estadoPago;
}

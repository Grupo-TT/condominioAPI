package com.condominio.dto.response;

import com.condominio.persistence.model.EstadoPago;
import com.condominio.persistence.model.TipoObligacion;
import com.condominio.persistence.model.TipoPago;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MultasPorCasaDTO {

    private Long id;
    private int casa;
    private String propietario;
    private String titulo;
    private int monto;
    private LocalDate fecha;
    private EstadoPago estadoPago;
    private TipoObligacion tipoObligacion;
    private String motivo;
    private TipoPago tipoPago;
}

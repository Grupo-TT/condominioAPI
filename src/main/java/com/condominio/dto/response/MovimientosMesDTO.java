package com.condominio.dto.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MovimientosMesDTO {
    private List<MovimientoDTO> movimientos;
    private MetricasDTO metricas;
}

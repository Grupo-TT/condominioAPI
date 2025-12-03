package com.condominio.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class MetricasDTO {
    private int ingresos;
    private int egresos;
    private int balance;
    private int saldoActual;
}

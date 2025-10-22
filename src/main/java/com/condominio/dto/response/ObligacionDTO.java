package com.condominio.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ObligacionDTO {

    private Long id;
    private String estado;
    private String motivo;
    private int casa;
    private int monto;
    private LocalDate fechaPago;
    private int saldo;
}

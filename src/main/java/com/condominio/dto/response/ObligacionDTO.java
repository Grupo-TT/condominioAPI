package com.condominio.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class ObligacionDTO {
    private String estado;
    private String motivo;
    private int casa;
    private int monto;
    private LocalDate fechaPago;
}

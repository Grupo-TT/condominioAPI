package com.condominio.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UltimoPagoDTO {
    private LocalDate fecha;
    private String concepto;
    private int valor;
    private boolean fueAbonoCompleto;
}

package com.condominio.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CasaSimpleDTO {
    private int numeroCasa;
    private String nombrePropietario;
    private boolean asistio;
}

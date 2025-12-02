package com.condominio.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@Data
@Getter
@AllArgsConstructor
public class AsistenciaDTO {
    private int numeroCasa;
    private boolean estado;
}

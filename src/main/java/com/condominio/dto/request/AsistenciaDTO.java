package com.condominio.dto.request;

import lombok.Data;
import lombok.Getter;

@Data
@Getter
public class AsistenciaDTO {
    private int numeroCasa;
    private boolean estado;
}

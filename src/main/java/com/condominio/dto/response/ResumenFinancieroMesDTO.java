package com.condominio.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ResumenFinancieroMesDTO {
    private String mes;
    private int entradas;
    private int salidas;
}

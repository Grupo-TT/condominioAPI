package com.condominio.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ResumenFinancieroDTO {
    private int year;
    private List<ResumenFinancieroMesDTO> meses;

}

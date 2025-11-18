package com.condominio.dto.request;

import lombok.Data;

@Data
public class MiembroBaseDTO {
    private Long idCasa;
    private String nombre;
    private Long numeroDocumento;
    private Long telefono;
    private String parentesco;
}

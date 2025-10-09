package com.condominio.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PersonaSimpleDTO {
    private String nombreCompleto;
    private Long telefono;
    private String correo;
}

package com.condominio.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MiembrosDTO {
    private String nombreCompleto;
    private String tipoMiembro;
    private Long numeroDocumento;
    private Long telefono;
    private String email;
}

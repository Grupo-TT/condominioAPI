package com.condominio.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CasaInfoMultaDTO {

    private int numeroCasa;
    private PersonaSimpleDTO propietario;
    private int cantidadMiembros;
    private int cantidadMascotas;
}

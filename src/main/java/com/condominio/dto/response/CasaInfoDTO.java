package com.condominio.dto.response;

import com.condominio.persistence.model.Persona;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CasaInfoDTO {

    private int numeroCasa;
    private PersonaSimpleDTO propietario;
    private int cantidadMiembros;
    private int cantidadMascotas;
}

package com.condominio.dto.response;

import com.condominio.persistence.model.EstadoFinancieroCasa;
import com.condominio.persistence.model.UsoCasa;
import lombok.*;

import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CasaInfoDTO {

    private int numeroCasa;
    private PersonaSimpleDTO propietario;
    private int cantidadMiembros;
    private int cantidadMascotas;
    private Map<String, Integer> mascotas;
    private UsoCasa usoCasa;
    private EstadoFinancieroCasa estadoFinancieroCasa;
}

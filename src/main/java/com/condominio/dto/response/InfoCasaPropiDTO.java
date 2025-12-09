package com.condominio.dto.response;

import com.condominio.persistence.model.UsoCasa;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InfoCasaPropiDTO {
    private int numeroCasa;
    private UsoCasa tipoUso;
    private int cantidadMiembros;
    private int cantidadMascotas;
}

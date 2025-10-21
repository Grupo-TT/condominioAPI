package com.condominio.dto.response;

import com.condominio.persistence.model.TipoMascota;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@NoArgsConstructor
@Getter
@Setter
public class MascotaCountDTO {
    private String tipo;
    private Long cantidad;

    public MascotaCountDTO(TipoMascota tipo, Long cantidad) {
        this.tipo = tipo.toString();
        this.cantidad = cantidad;
    }
}

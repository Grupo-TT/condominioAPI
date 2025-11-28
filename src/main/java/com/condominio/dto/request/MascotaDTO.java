package com.condominio.dto.request;

import com.condominio.persistence.model.TipoMascota;
import lombok.Data;

@Data
public class MascotaDTO {
    private TipoMascota tipoMascota;
    private short cantidad;
    private Long idCasa;
}

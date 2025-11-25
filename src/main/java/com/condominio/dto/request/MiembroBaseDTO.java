package com.condominio.dto.request;

import com.condominio.persistence.model.TipoDocumento;
import lombok.Data;

@Data
public class MiembroBaseDTO {
    private Long idCasa;
    private String nombre;
    private Long numeroDocumento;
    private Long telefono;
    private TipoDocumento tipoDocumento;
    private String parentesco;
}

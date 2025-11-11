package com.condominio.dto.response;

import com.condominio.dto.request.MiembroBaseDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class MiembrosDatosDTO extends MiembroBaseDTO {
    private Long id;
}

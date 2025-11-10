package com.condominio.dto.request;

import com.condominio.persistence.model.TipoDocumento;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PersonaUpdateDTO {
    @NotNull private String primerNombre;
    @NotNull  private String segundoNombre;
    @NotNull private String primerApellido;
    @NotNull private String segundoApellido;
    @NotNull private Long telefono;
    @NotNull private TipoDocumento tipoDocumento;
    @NotNull private Long numeroDocumento;
}

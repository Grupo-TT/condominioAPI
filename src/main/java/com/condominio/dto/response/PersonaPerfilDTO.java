package com.condominio.dto.response;

import com.condominio.persistence.model.TipoDocumento;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PersonaPerfilDTO {


    private String primerNombre;
    private String segundoNombre;
    private String primerApellido;
    private String segundoApellido;
    private TipoDocumento tipoDocumento;
    private Long numeroDocumento;
    private Long telefono;
    private String email;
    private Boolean junta;
    private Boolean comiteConvivencia;
    private int numeroCasa;
}

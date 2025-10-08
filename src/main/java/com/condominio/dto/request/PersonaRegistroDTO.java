package com.condominio.dto.request;

import com.condominio.persistence.model.RoleEnum;
import com.condominio.persistence.model.TipoDocumento;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PersonaRegistroDTO {

    private String primerNombre;
    private String segundoNombre;
    private String primerApellido;
    private String segundoApellido;
    private TipoDocumento tipoDocumento;
    private Long numeroDocumento;
    private Long telefono;
    private Long idCasa;
    private RoleEnum rolEnCasa;
    @Email
    private String email;
}

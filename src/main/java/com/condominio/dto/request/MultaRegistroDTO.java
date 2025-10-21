package com.condominio.dto.request;

import com.condominio.persistence.model.RoleEnum;
import com.condominio.persistence.model.TipoDocumento;
import com.condominio.persistence.model.TipoPago;
import jakarta.persistence.Column;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MultaRegistroDTO {

    private Long idCasa;
    private int monto;
    private String motivo;
}

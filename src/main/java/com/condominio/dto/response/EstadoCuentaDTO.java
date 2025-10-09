package com.condominio.dto.response;

import com.condominio.persistence.model.Obligacion;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EstadoCuentaDTO {
    private int numeroCasa;
    private PersonaSimpleDTO propietario;
    private Long saldoPendienteTotal;
    private List<Obligacion> deudasActivas;
}

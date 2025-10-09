package com.condominio.dto.response;

import com.condominio.persistence.model.Obligacion;
import com.condominio.persistence.model.Persona;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EstadoCuentaDTO {
    private int numeroCasa;
    private Persona propietario;
    private Long saldoPendienteTotal;
    private List<Obligacion> deudasActivas;
}

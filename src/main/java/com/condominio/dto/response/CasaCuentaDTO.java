package com.condominio.dto.response;

import com.condominio.persistence.model.Obligacion;
import lombok.Builder;
import lombok.Data;

import java.util.List;
@Data
@Builder
public class CasaCuentaDTO {
    private Long saldoPendienteTotal;
    private List<Obligacion> multasActivas;
}

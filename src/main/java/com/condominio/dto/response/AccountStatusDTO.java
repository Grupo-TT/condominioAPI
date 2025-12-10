package com.condominio.dto.response;

import com.condominio.persistence.model.EstadoFinancieroCasa;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AccountStatusDTO {
    private int saldoPendiente;
    private EstadoFinancieroCasa estadoCasa; // "EN_DIA" o "EN_MORA"
    private UltimoPagoDTO ultimoPago;
}

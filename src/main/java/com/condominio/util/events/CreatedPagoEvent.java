package com.condominio.util.events;

import com.condominio.dto.response.ObligacionDTO;
import lombok.Getter;

@Getter
public class CreatedPagoEvent {
    private final String emailPropietario;
    private final ObligacionDTO obligacion;

    public CreatedPagoEvent(String emailPropietario, ObligacionDTO obligacion) {
        this.emailPropietario = emailPropietario;
        this.obligacion = obligacion;
    }
}

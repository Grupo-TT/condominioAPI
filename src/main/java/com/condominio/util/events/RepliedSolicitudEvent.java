package com.condominio.util.events;

import com.condominio.dto.response.SolicitudReservaRecursoDTO;
import lombok.Getter;

@Getter
public class RepliedSolicitudEvent {

    private final String emailPropietario;
    private final SolicitudReservaRecursoDTO solicitudReservaRecursoDTO;

    public RepliedSolicitudEvent(String emailPropietario, SolicitudReservaRecursoDTO solicitudReservaRecursoDTO) {
        this.emailPropietario = emailPropietario;
        this.solicitudReservaRecursoDTO = solicitudReservaRecursoDTO;
    }
}

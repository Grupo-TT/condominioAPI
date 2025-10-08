package com.condominio.util.events;

import com.condominio.persistence.model.Persona;
import lombok.Getter;

@Getter
public class CreatedPersonaEvent {
    private final Persona persona;
    public CreatedPersonaEvent(Persona persona) {
        this.persona = persona;
    }

}

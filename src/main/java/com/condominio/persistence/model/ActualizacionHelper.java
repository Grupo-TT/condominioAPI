package com.condominio.persistence.model;

import com.condominio.persistence.repository.PersonaRepository;
import com.condominio.persistence.repository.UserRepository;
import com.condominio.util.constants.AppConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

@Component
@RequiredArgsConstructor
public class ActualizacionHelper {

    private final UserRepository userRepository;
    private final PersonaRepository personaRepository;

    public <T extends Cargo> T aplicarDatosComunes(T entidad, double nuevoValor) {
        return aplicarDatosComunes(entidad, nuevoValor, false);
    }

    public <T extends Cargo> T aplicarDatosComunes(T entidad, double nuevoValor, boolean dividirPorCien) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        UserEntity userEntity = userRepository.findUserEntityByEmail(username);
        Persona persona = personaRepository.findPersonaByUser(userEntity);

        entidad.setValorActual(entidad.getNuevoValor());
        entidad.setNuevoValor(dividirPorCien ? nuevoValor / 100 : nuevoValor);
        entidad.setCorreoActualizador(username);
        entidad.setNombreActualizador(persona.getNombreCompleto());
        entidad.setFechaAplicacion(OffsetDateTime.now(AppConstants.ZONE));

        return entidad;
    }
}

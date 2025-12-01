package com.condominio.util.helper;

import com.condominio.dto.response.PersonaSimpleDTO;
import com.condominio.persistence.model.Persona;
import com.condominio.persistence.repository.PersonaRepository;
import com.condominio.util.exception.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PersonaHelper {

    private final PersonaRepository personaRepository;

    public Persona obtenerSolicitantePorCasa(Long casaId) {
        return personaRepository.findArrendatarioByCasaId(casaId)
                .orElseGet(() -> personaRepository.findPropietarioByCasaId(casaId)
                        .orElseThrow(() -> new ApiException(
                                "No se encontró un solicitante (arrendatario o propietario) para la casa con ID " + casaId,
                                HttpStatus.BAD_REQUEST
                        )));
    }

    public PersonaSimpleDTO toPersonaSimpleDTO(Persona persona) {
        return PersonaSimpleDTO.builder()
                .nombreCompleto(persona.getNombreCompleto())
                .telefono(persona.getTelefono())
                .correo(persona.getUser().getEmail())
                .build();
    }
}

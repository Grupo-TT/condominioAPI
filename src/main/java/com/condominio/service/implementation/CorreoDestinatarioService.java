package com.condominio.service.implementation;

import com.condominio.dto.response.DestinatarioInfoDTO;
import com.condominio.persistence.model.CorreoDestinatario;
import com.condominio.persistence.model.Persona;
import com.condominio.persistence.model.UserEntity;
import com.condominio.persistence.repository.CorreoDestinatarioRepository;
import com.condominio.persistence.repository.PersonaRepository;
import com.condominio.persistence.repository.UserRepository;
import com.condominio.util.exception.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CorreoDestinatarioService {

    private final CorreoDestinatarioRepository correoDestinatarioRepository;
    private final UserRepository userRepository;
    private final PersonaRepository personaRepository;

    public List<DestinatarioInfoDTO> getDestinatariosInfo(Long correoId) {

        List<CorreoDestinatario> lista = correoDestinatarioRepository.findByCorreoEnviadoId(correoId);

        if (lista.isEmpty()) {
            throw new ApiException("No se encontraron destinatarios para este correo", HttpStatus.NOT_FOUND);
        }
        List<DestinatarioInfoDTO> result = new ArrayList<>();

        for (CorreoDestinatario dest : lista) {


            String email = dest.getEmailDestinatario();

            UserEntity user = userRepository.findByEmail(email)
                    .orElse(null);

            if (user == null) {
                continue;
            }


            Persona persona = personaRepository.findByUser_Id(user.getId());
            if (persona == null) {
                continue;
            }


            DestinatarioInfoDTO dto = new DestinatarioInfoDTO(
                    persona.getNombreCompleto(),
                    persona.getCasa().getId(),
                    email
            );

            result.add(dto);
        }

        return result;
    }
}

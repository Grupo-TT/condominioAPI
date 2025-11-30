package com.condominio.service.implementation;

import com.condominio.dto.response.DestinatarioInfoDTO;
import com.condominio.persistence.model.CorreoEnviado;
import com.condominio.persistence.model.Persona;
import com.condominio.persistence.model.UserEntity;
import com.condominio.persistence.repository.CorreoEnviadoRepository;
import com.condominio.persistence.repository.PersonaRepository;
import com.condominio.persistence.repository.UserRepository;
import com.condominio.util.exception.ApiException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CorreoDestinatarioService {

    private final CorreoEnviadoRepository correoEnviadoRepository;
    private final UserRepository userRepository;
    private final PersonaRepository personaRepository;
    private final ObjectMapper objectMapper;

    public List<DestinatarioInfoDTO> getDestinatariosInfo(Long correoId) {
        CorreoEnviado correoEnviado = correoEnviadoRepository.findById(correoId)
                .orElseThrow(() -> new ApiException("Correo no encontrado", HttpStatus.NOT_FOUND));

        String destinatariosJson = correoEnviado.getDestinatarios();
        if (destinatariosJson == null || destinatariosJson.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> emails;
        try {
            emails = objectMapper.readValue(destinatariosJson, new TypeReference<List<String>>() {});
        } catch (IOException e) {
            // Consider logging the error
            throw new ApiException("Error al procesar la lista de destinatarios", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        if (emails.isEmpty()) {
            throw new ApiException("No se encontraron destinatarios para este correo", HttpStatus.NOT_FOUND);
        }

        List<DestinatarioInfoDTO> result = new ArrayList<>();
        for (String email : emails) {
            UserEntity user = userRepository.findByEmail(email).orElse(null);
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
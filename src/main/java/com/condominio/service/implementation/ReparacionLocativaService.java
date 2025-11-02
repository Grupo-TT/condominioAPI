package com.condominio.service.implementation;

import com.condominio.dto.response.ReparacionLocativaDTO;
import com.condominio.persistence.model.Persona;
import com.condominio.persistence.model.ReparacionLocativa;
import com.condominio.persistence.repository.ReparacionLocativaRepository;
import com.condominio.service.interfaces.IReparacionLocativaService;
import com.condominio.util.exception.ApiException;
import com.condominio.util.helper.PersonaHelper;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReparacionLocativaService implements IReparacionLocativaService {

    private final ReparacionLocativaRepository reparacionLocativaRepository;
    private final PersonaHelper personaHelper;
    private final ModelMapper modelMapper;



    @Override
    public List<ReparacionLocativaDTO> findAll() {
        List<ReparacionLocativa> reparaciones = reparacionLocativaRepository.findAll();
        List<ReparacionLocativaDTO> dtos = reparaciones.stream().map(reparacion -> {
            ReparacionLocativaDTO dto = modelMapper.map(reparacion, ReparacionLocativaDTO.class);

            Long casaId = reparacion.getSolicitudReparacionLocativa().getCasa().getId();
            Persona solicitante = personaHelper.obtenerSolicitantePorCasa(casaId);

            dto.getSolicitudReparacionLocativa().setSolicitante(personaHelper.toPersonaSimpleDTO(solicitante));

            return dto;
        }).toList();

        if (reparaciones.isEmpty()) {
            throw new ApiException("No hay reparaciones registradas", HttpStatus.NOT_FOUND);
        }
        return dtos;
    }
}

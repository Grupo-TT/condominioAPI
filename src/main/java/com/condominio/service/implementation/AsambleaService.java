package com.condominio.service.implementation;

import com.condominio.dto.request.AsambleaDTO;
import com.condominio.dto.response.SuccessResult;
import com.condominio.persistence.model.Asamblea;
import com.condominio.persistence.model.EstadoAsamblea;
import com.condominio.persistence.model.Persona;
import com.condominio.persistence.repository.AsambleaRepository;
import com.condominio.persistence.repository.PersonaRepository;
import com.condominio.service.interfaces.IAsambleaService;
import com.condominio.util.constants.AppConstants;
import com.condominio.util.exception.ApiException;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
public class AsambleaService implements IAsambleaService {

    private final AsambleaRepository asambleaRepository;
    private final ModelMapper modelMapper;
    private final PersonaRepository personaRepository;
    private final EmailService emailService;


    @Override
    public SuccessResult<AsambleaDTO> create(AsambleaDTO asamblea) {
        Date todayInBogota = Date.from(
                LocalDate.now(AppConstants.ZONE)
                        .atStartOfDay(AppConstants.ZONE)
                        .toInstant()
        );
        if (asamblea.getFecha().before(todayInBogota)){
            throw new ApiException("Por favor ingresar una fecha valida", HttpStatus.BAD_REQUEST);
        }
        Asamblea newAsamblea = modelMapper.map(asamblea, Asamblea.class);
        newAsamblea.setEstado(EstadoAsamblea.PROGRAMADA);
        newAsamblea = asambleaRepository.save(newAsamblea);

        Iterable<Persona> iterable = personaRepository.findAll();
        List<Persona> personas = StreamSupport.stream(iterable.spliterator(), false)
                .collect(Collectors.toList());

        emailService.enviarInvitacionesAsambleaMasivas(personas, newAsamblea);
        return new SuccessResult<>("Asamblea programada correctamente",asamblea);
    }
}

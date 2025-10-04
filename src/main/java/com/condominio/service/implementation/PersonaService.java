package com.condominio.service.implementation;

import com.condominio.dto.request.PersonaRegistroDTO;
import com.condominio.dto.response.SuccessResult;
import com.condominio.persistence.model.Casa;
import com.condominio.persistence.model.Persona;
import com.condominio.persistence.model.RoleEnum;
import com.condominio.persistence.model.UserEntity;
import com.condominio.persistence.repository.PersonaRepository;
import com.condominio.service.interfaces.ICasaService;
import com.condominio.service.interfaces.IPersonaService;
import com.condominio.service.interfaces.IUserService;
import com.condominio.util.events.CreatedPersonaEvent;
import com.condominio.util.exception.ApiException;
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class PersonaService implements IPersonaService {

    private final IUserService userService;
    private final PersonaRepository personaRepository;
    private final ICasaService casaService;
    private final ModelMapper modelMapper;
    private final ApplicationEventPublisher applicationEventPublisher;

    public PersonaService(IUserService userService,
                          PersonaRepository personaRepository,
                          ModelMapper modelMapper,
                          ICasaService casaService,
                          ApplicationEventPublisher applicationEventPublisher

    ) {
        this.userService = userService;
        this.personaRepository = personaRepository;
        this.casaService = casaService;
        this.modelMapper = modelMapper;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public Boolean existsByNumeroDeDocumento(Long numeroDeDocumento) {
        return personaRepository.findByNumeroDocumento(numeroDeDocumento).isPresent();
    }

    @Override
    public boolean existsRoleInCasa(Long casaId, RoleEnum roleEnum) {
        return personaRepository.existsRoleInCasa(casaId, roleEnum);
    }

    @Override
    @Transactional
    public SuccessResult<Persona> save(PersonaRegistroDTO persona) {
        UserEntity userEntity = userService.createUser(persona.getEmail(),
                persona.getNumeroDocumento(),
                persona.getRolEnCasa());

        if (existsByNumeroDeDocumento(persona.getNumeroDocumento())) {
            throw new ApiException("El numero  de documento " +
                    "ya se  encuentra registrado", HttpStatus.BAD_REQUEST);
        }

        boolean takenRol = existsRoleInCasa(persona.getIdCasa(),
                persona.getRolEnCasa());

        if (takenRol) {
            throw new ApiException("Ya existe un " + persona.getRolEnCasa()
                    + " registrado para esta casa",
                    HttpStatus.BAD_REQUEST);
        }
        Casa findCasa = casaService.findById(persona.getIdCasa())
                .orElseThrow(() -> new ApiException(
                        "La casa con id " + persona.getIdCasa() + " no existe",
                        HttpStatus.NOT_FOUND
                ));

        Persona newPersona = modelMapper.map(persona, Persona.class);
        newPersona.setUser(userEntity);
        newPersona.setEstado(true);
        newPersona.setComiteConvivencia(false);
        newPersona.setJunta(false);
        newPersona.setCasa(findCasa);
        Persona savedPersona = personaRepository.save(newPersona);
        applicationEventPublisher.publishEvent(new CreatedPersonaEvent(savedPersona));
        return new SuccessResult<>("Persona registrada correctamente", savedPersona);
    }

}

package com.condominio.service.implementation;

import com.condominio.dto.request.PersonaRegistroDTO;
import com.condominio.dto.request.PersonaUpdateDTO;
import com.condominio.dto.response.PersonaPerfilDTO;
import com.condominio.dto.response.SuccessResult;
import com.condominio.persistence.model.Casa;
import com.condominio.persistence.model.Persona;
import com.condominio.persistence.model.RoleEnum;
import com.condominio.persistence.model.UserEntity;
import com.condominio.persistence.repository.PersonaRepository;
import com.condominio.persistence.repository.UserRepository;
import com.condominio.service.interfaces.ICasaService;
import com.condominio.service.interfaces.IPersonaService;
import com.condominio.service.interfaces.IUserService;
import com.condominio.util.events.CreatedPersonaEvent;
import com.condominio.util.exception.ApiException;
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;



@Service
public class PersonaService implements IPersonaService {

    private final IUserService userService;
    private final PersonaRepository personaRepository;
    private final ICasaService casaService;
    private final ModelMapper modelMapper;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final UserRepository userRepository;

    public PersonaService(IUserService userService,
                          PersonaRepository personaRepository,
                          ModelMapper modelMapper,
                          ICasaService casaService,
                          ApplicationEventPublisher applicationEventPublisher,
                          UserRepository userRepository

    ) {
        this.userService = userService;
        this.personaRepository = personaRepository;
        this.casaService = casaService;
        this.modelMapper = modelMapper;
        this.applicationEventPublisher = applicationEventPublisher;
        this.userRepository = userRepository;
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

    public Persona obtenerSolicitantePorCasa(Long idCasa) {
        return personaRepository.findArrendatarioByCasaId(idCasa)
                .orElseGet(() -> personaRepository.findPropietarioByCasaId(idCasa)
                        .orElseThrow(() -> new ApiException(
                                "No se encontró un solicitante (arrendatario o propietario) para la casa con ID " + idCasa,
                                HttpStatus.BAD_REQUEST
                        )));
    }

    public Persona getPersonaFromUserDetails(UserDetails userDetails) {
        String email = userDetails.getUsername();
        UserEntity userEntity = userRepository.findUserEntityByEmail(email);

        if (userEntity == null) {
            throw new ApiException("Usuario no encontrado con el email: "
                    + email,HttpStatus.NOT_FOUND);
        }

        Persona persona = personaRepository.findPersonaByUser(userEntity);
        if (persona == null) {
            throw new ApiException("Persona no encontrada para el usuario: "
                    + email,HttpStatus.NOT_FOUND);
        }

        return persona;
    }

    public PersonaPerfilDTO getPersonaPerfil( UserDetails userDetails) {
        Persona persona = getPersonaFromUserDetails(userDetails);
        return PersonaPerfilDTO.builder()
                .numeroCasa(persona.getCasa().getNumeroCasa())
                .junta(persona.getJunta())
                .comiteConvivencia(persona.getComiteConvivencia())
                .numeroDocumento(persona.getNumeroDocumento())
                .primerApellido(persona.getPrimerApellido())
                .segundoApellido(persona.getSegundoApellido())
                .primerNombre(persona.getPrimerNombre())
                .segundoNombre(persona.getSegundoNombre())
                .email(persona.getUser().getEmail())
                .telefono(persona.getTelefono())
                .tipoDocumento(persona.getTipoDocumento())
                .build();
    }

    public SuccessResult<?> updatePersona(PersonaUpdateDTO personaUpdate,UserDetails userDetails){
        Persona persona = getPersonaFromUserDetails(userDetails);

        if (personaRepository.existsByNumeroDocumentoAndIdNot(personaUpdate.getNumeroDocumento(), persona.getId())) {
            throw new ApiException("El número de documento ya está registrado",HttpStatus.BAD_REQUEST);
        }

        persona.setPrimerNombre(personaUpdate.getPrimerNombre());
        persona.setSegundoNombre(personaUpdate.getSegundoNombre());
        persona.setPrimerApellido(personaUpdate.getPrimerApellido());
        persona.setSegundoApellido(personaUpdate.getSegundoApellido());
        persona.setTipoDocumento(personaUpdate.getTipoDocumento());
        persona.setNumeroDocumento(personaUpdate.getNumeroDocumento());
        persona.setTelefono(personaUpdate.getTelefono());
        personaRepository.save(persona);
        return new SuccessResult<>("Persona actualizada correctamente",null);

    }

}

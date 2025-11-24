package com.condominio.service.implementation;

import com.condominio.dto.request.PersonaRegistroDTO;
import com.condominio.dto.request.PersonaUpdateDTO;
import com.condominio.dto.response.PersonaPerfilDTO;
import com.condominio.dto.response.PersonaSimpleRolDTO;
import com.condominio.dto.response.PersonaSimpleRolDTO;
import com.condominio.dto.response.SuccessResult;
import com.condominio.persistence.model.*;
import com.condominio.persistence.repository.MascotaRepository;
import com.condominio.persistence.repository.MiembroRepository;
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

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import java.util.Optional;

@Service
public class PersonaService implements IPersonaService {

    private final IUserService userService;
    private final PersonaRepository personaRepository;
    private final ICasaService casaService;
    private final ModelMapper modelMapper;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final UserRepository userRepository;
    private final MascotaRepository mascotaRepository ;
    private final MiembroService miembroService;
    private final MiembroRepository miembroRepository;

    public PersonaService(IUserService userService,
                          PersonaRepository personaRepository,
                          ModelMapper modelMapper,
                          ICasaService casaService,
                          ApplicationEventPublisher applicationEventPublisher,
                          UserRepository userRepository, MascotaRepository mascotaRepository, MiembroService miembroService,

                          MiembroRepository miembroRepository) {
        this.userService = userService;
        this.personaRepository = personaRepository;
        this.casaService = casaService;
        this.modelMapper = modelMapper;
        this.applicationEventPublisher = applicationEventPublisher;
        this.userRepository = userRepository;
        this.mascotaRepository = mascotaRepository;
        this.miembroService = miembroService;
        this.miembroRepository = miembroRepository;
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
        List<Persona> personasCasa = personaRepository.findAllByCasa_Id(persona.getIdCasa());
        if (!personasCasa.isEmpty()) {
            for(Persona propietarioAntiguo : personasCasa){
                boolean tieneRol = propietarioAntiguo.getUser().getRoles()
                        .stream()
                        .anyMatch(r -> r.getRoleEnum().equals(persona.getRolEnCasa()));
                if(tieneRol){
                    propietarioAntiguo.setEstado(false);
                    mascotaRepository.deleteAllByCasa(propietarioAntiguo.getCasa());
                    miembroRepository.deleteAllByCasa(propietarioAntiguo.getCasa());
                    propietarioAntiguo.getUser().setEnabled(false);
                    propietarioAntiguo.setCasa(null);
                    personaRepository.save(propietarioAntiguo);
                    System.out.println("Llegó bien antes de registarPersona.");
                    Persona savedPersona = registrarPersona(persona);
                    System.out.println("Llegó bien hasta el final.");
                    return new SuccessResult<>("Persona registrada correctamente", savedPersona);
                }
            }
        }
        Persona savedPersona = registrarPersona(persona);
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

    public SuccessResult<Void> updatePersona(PersonaUpdateDTO personaUpdate,UserDetails userDetails){
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
    public List<PersonaSimpleRolDTO> obtenerTodasPersonas() {
        Iterable<Persona> personas = personaRepository.findAll();
        return StreamSupport.stream(personas.spliterator(), false)
                .map(this::convertirASimpleRolDTO)
                .collect(Collectors.toList());
        }
    private PersonaSimpleRolDTO convertirASimpleRolDTO(Persona persona) {
        PersonaSimpleRolDTO dto = new PersonaSimpleRolDTO();
        dto.setNombreCompleto(persona.getNombreCompleto());
        dto.setTelefono(persona.getTelefono());
        dto.setCorreo(persona.getUser().getEmail());
        List<String> roles = persona.getUser().getRoles().stream()
                .map(role -> role.getRoleEnum().name())
                .toList();
        dto.setRoles(roles);
        return dto;
    }
    public Persona registrarPersona(PersonaRegistroDTO persona){

        if (existsByNumeroDeDocumento(persona.getNumeroDocumento())) {
            throw new ApiException("El numero  de documento " +
                    "ya se  encuentra registrado", HttpStatus.BAD_REQUEST);
        }

        UserEntity userEntity = userService.createUser(persona.getEmail(),
                persona.getNumeroDocumento(),
                persona.getRolEnCasa());

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
        return savedPersona;
    }

}

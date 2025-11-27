package com.condominio.service.implementation;

import com.condominio.dto.request.PqrsUpdateDTO;
import com.condominio.dto.response.PqrsDTO;
import com.condominio.dto.response.PqrsPropiDTO;
import com.condominio.dto.response.SuccessResult;
import com.condominio.persistence.model.*;
import com.condominio.persistence.model.PqrsEntity;
import com.condominio.persistence.repository.PqrsRepository;
import com.condominio.persistence.repository.PersonaRepository;
import com.condominio.service.interfaces.IPqrsService;
import com.condominio.util.exception.ApiException;
import com.condominio.util.helper.PersonaHelper;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PqrsService implements IPqrsService {

    private final PqrsRepository pqrsRepository;
    private final ModelMapper modelMapper;
    private final PersonaHelper personaHelper;
    private final PersonaRepository personaRepository;


    @Override
    public SuccessResult<List<PqrsDTO>> findByEstado(EstadoPqrs estado) {
        List<PqrsEntity> listaPqrs =
                pqrsRepository.findByEstadoPqrs(estado);

        if (listaPqrs.isEmpty()) {
            throw new ApiException("No hay PQRS con estado: " + estado, HttpStatus.OK);
        }

        List<PqrsDTO> dtos = listaPqrs.stream().map(pqrsEntity -> {
            PqrsDTO dto = modelMapper.map(pqrsEntity, PqrsDTO.class);

            Long casaId = pqrsEntity.getCasa().getId();
            Persona solicitante = personaHelper.obtenerSolicitantePorCasa(casaId);
            dto.setSolicitante(personaHelper.toPersonaSimpleDTO(solicitante));

            return dto;
        }).toList();

        return new SuccessResult<>("PQRS " + estado.name().toLowerCase() + " obtenidas correctamente", dtos);
    }

    @Override
    public SuccessResult<PqrsUpdateDTO> update(Long id, PqrsUpdateDTO pqrs) {
        PqrsEntity oldPqrs = pqrsRepository.findById(id)
                .orElseThrow(() -> new ApiException("No se ha encontrado la PQRS", HttpStatus.NOT_FOUND));

        if(pqrs.getFechaRealizacion().isAfter(LocalDate.now())) {
            throw new ApiException("Por favor, ingresa una fecha y hora validas", HttpStatus.BAD_REQUEST);
        }

        oldPqrs.setFechaRealizacion(pqrs.getFechaRealizacion());
        oldPqrs.setEstadoPqrs(pqrs.getEstadoPqrs());
        oldPqrs.setTitulo(pqrs.getTitulo());
        oldPqrs.setDescripcion(pqrs.getDescripcion());
        oldPqrs.setTipoPqrs(pqrs.getTipoPqrs());

        PqrsEntity actualizada = pqrsRepository.save(oldPqrs);
        PqrsUpdateDTO dto = modelMapper.map(actualizada, PqrsUpdateDTO.class);

        return new SuccessResult<>("PQRS modificada exitosamente", dto);
    }

    @Override
    public SuccessResult<PqrsDTO> marcarRevisada(Long id) {
        PqrsEntity pqrsEntity = pqrsRepository.findById(id)
                .orElseThrow(() -> new ApiException("No se ha encontrado la PQRS", HttpStatus.NOT_FOUND));
        Long casaId = pqrsEntity.getCasa().getId();
        Persona solicitante = personaHelper.obtenerSolicitantePorCasa(casaId);

        if(pqrsEntity.getEstadoPqrs()==EstadoPqrs.REVISADA){
            throw new ApiException("La PQRS ya esta marcada como revisada revisada", HttpStatus.BAD_REQUEST);
        }

        pqrsEntity.setEstadoPqrs(EstadoPqrs.REVISADA);
        PqrsEntity actualizada = pqrsRepository.save(pqrsEntity);
        PqrsDTO dto = modelMapper.map(actualizada, PqrsDTO.class);
        dto.setSolicitante(personaHelper.toPersonaSimpleDTO(solicitante));

        return new SuccessResult<>("PQRS marcada como revisada exitosamente", dto);
    }

    @Override
    public SuccessResult<PqrsDTO> eliminar(Long id) {

        PqrsEntity pqrsEntity = verificarUsuarioAndPqrs(id);
        Long casaId = pqrsEntity.getCasa().getId();
        Persona solicitante = personaHelper.obtenerSolicitantePorCasa(casaId);
        PqrsDTO dto = modelMapper.map(pqrsEntity, PqrsDTO.class);
        dto.setSolicitante(personaHelper.toPersonaSimpleDTO(solicitante));

        if(pqrsEntity.getEstadoPqrs()==EstadoPqrs.REVISADA){
            throw new ApiException("No se puede eliminar una PQRS que ya está marcada como revisada", HttpStatus.BAD_REQUEST);
        }

        pqrsRepository.delete(pqrsEntity);
        return new SuccessResult<>("PQRS eliminada exitosamente", dto);
    }

    @Override
    public SuccessResult<PqrsPropiDTO> crearPqrs(PqrsPropiDTO pqrs) {

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Persona solicitante = personaRepository.findByUserEmail(username)
                .orElseThrow(() -> new ApiException("Usuario no encontrado", HttpStatus.NOT_FOUND));

        PqrsEntity pqrsEntity = PqrsEntity.builder()
                .estadoPqrs(EstadoPqrs.PENDIENTE)
                .fechaRealizacion(LocalDate.now())
                .tipoPqrs(pqrs.getTipoPqrs())
                .titulo(pqrs.getTitulo())
                .descripcion(pqrs.getDescripcion())
                .casa(solicitante.getCasa())
                .build();
        pqrsRepository.save(pqrsEntity);

        return new SuccessResult<>("PQRS creado exitosamente", pqrs) ;
    }

    @Override
    public SuccessResult<PqrsPropiDTO> modificarPqrs(Long id, PqrsPropiDTO pqrs) {
        PqrsEntity pqrsEntity = verificarUsuarioAndPqrs(id);

        if(pqrsEntity.getEstadoPqrs()==EstadoPqrs.REVISADA){
            throw new ApiException("No se puede modificar una PQRS que ya está marcarda como revisada", HttpStatus.BAD_REQUEST);
        }

        pqrsEntity.setTitulo(pqrs.getTitulo());
        pqrsEntity.setDescripcion(pqrs.getDescripcion());
        pqrsEntity.setTipoPqrs(pqrs.getTipoPqrs());
        pqrsEntity.setFechaRealizacion(LocalDate.now());
        pqrsRepository.save(pqrsEntity);

        return new SuccessResult<>("PQRS modificado exitosamente", pqrs);
    }

    public PqrsEntity verificarUsuarioAndPqrs(Long id) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Persona persona = personaRepository.findByUserEmail(username)
                .orElseThrow(() -> new ApiException("Usuario no encontrado", HttpStatus.NOT_FOUND));

        PqrsEntity pqrsEntity = pqrsRepository.findById(id)
                .orElseThrow(() -> new ApiException("No se ha encontrado la PQRS", HttpStatus.NOT_FOUND));

        Long casaPqrsId = pqrsEntity.getCasa() != null ? pqrsEntity.getCasa().getId() : null;
        Long casaUsuarioId = persona.getCasa() != null ? persona.getCasa().getId() : null;

        if (casaUsuarioId == null || !casaUsuarioId.equals(casaPqrsId)) {
            throw new ApiException("No autorizado para modificar esta PQRS", HttpStatus.FORBIDDEN);
        }
        return pqrsEntity;
    }

}

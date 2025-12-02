package com.condominio.service.implementation;

import com.condominio.dto.request.AsambleaDTO;
import com.condominio.dto.response.AsambleaConAsistenciaDTO;
import com.condominio.dto.response.CasaSimpleDTO;
import com.condominio.dto.response.SuccessResult;
import com.condominio.persistence.model.Asamblea;
import com.condominio.persistence.model.Asistencia;
import com.condominio.persistence.model.EstadoAsamblea;
import com.condominio.persistence.model.Persona;
import com.condominio.persistence.repository.AsambleaRepository;
import com.condominio.persistence.repository.AsistenciaRepository;
import com.condominio.persistence.repository.PersonaRepository;
import com.condominio.service.interfaces.IAsambleaService;
import com.condominio.util.constants.AppConstants;
import com.condominio.util.exception.ApiException;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
public class AsambleaService implements IAsambleaService {

    private final AsambleaRepository asambleaRepository;
    private final ModelMapper modelMapper;
    private final PersonaRepository personaRepository;
    private final EmailService emailService;
    private final AsistenciaRepository asistenciaRepository;


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

        List<Persona> propietarios = personaRepository.findAllPropietariosConCasa();
        List<Asistencia> asistencias = new ArrayList<>();
        for (Persona propietario : propietarios) {
            Asistencia asistencia = new Asistencia();
            asistencia.setAsamblea(newAsamblea);
            asistencia.setEstado(false);
            asistencia.setFecha(todayInBogota);
            asistencia.setCasa(propietario.getCasa());
            asistencia.setNombreResponsable(propietario.getNombreCompleto());
            asistencias.add(asistencia);
        }
        System.out.println("Asistencias: " + asistencias);
        asistenciaRepository.saveAll(asistencias);

        Iterable<Persona> iterable = personaRepository.findAll();
        List<Persona> personas = StreamSupport.stream(iterable.spliterator(), false)
                .collect(Collectors.toList());

        emailService.enviarInvitacionesAsambleaMasivas(personas, newAsamblea);
        return new SuccessResult<>("Asamblea programada correctamente",asamblea);
    }

    @Override
    public SuccessResult<List<Asamblea>> findAllAsambleas() {
        List<Asamblea> asambleas = (List<Asamblea>) asambleaRepository.findAll();
        if (asambleas.isEmpty()) {
            throw new ApiException("No se encontraron registros.", HttpStatus.OK);
        }
        return new SuccessResult<>("No se encontraron registros.", asambleas);
    }

    @Override
    public SuccessResult<AsambleaDTO> edit(AsambleaDTO asambleaEdit, Long id) {
        Optional<Asamblea> asambleaOptional = asambleaRepository.findById(id);
        if (asambleaOptional.isPresent()) {
            Asamblea asamblea = asambleaOptional.get();
            asamblea.setEstado(asambleaEdit.getEstado());
            asamblea.setFecha(asambleaEdit.getFecha());
            asamblea.setLugar(asambleaEdit.getLugar());
            asamblea.setHoraInicio(asambleaEdit.getHoraInicio());
            asamblea.setTitulo(asambleaEdit.getTitulo());
            asamblea.setDescripcion(asambleaEdit.getDescripcion());
            asambleaRepository.save(asamblea);
            return new SuccessResult<>("Se actualizó la asamblea satisfactoriamente.", asambleaEdit);
        }
        throw new ApiException("No se pudo actualizar el registro.", HttpStatus.BAD_REQUEST);
    }

    @Override
    public SuccessResult<Void> delete(Long id) {
        asambleaRepository.deleteById(id);
        return new SuccessResult<>("Se eliminó la asamblea satisfactoriamente.", null);
    }

    @Override
    public SuccessResult<AsambleaConAsistenciaDTO> getAsambleaById(Long id) {
        Optional<Asamblea> asambleaOptional = asambleaRepository.findById(id);
        if (asambleaOptional.isPresent()) {
            Asamblea asamblea = asambleaOptional.get();
            List<Asistencia> asistencias = asistenciaRepository.findAllByAsamblea(asamblea);
            List<CasaSimpleDTO> propietarios = asistencias.stream().map(asistencia -> new CasaSimpleDTO(
                    asistencia.getCasa().getNumeroCasa(), asistencia.getNombreResponsable(), asistencia.getEstado()
            )).toList();
            AsambleaConAsistenciaDTO dto = AsambleaConAsistenciaDTO.builder()
                    .id(asamblea.getId())
                    .titulo(asamblea.getTitulo())
                    .descripcion(asamblea.getDescripcion())
                    .fecha(asamblea.getFecha())
                    .estado(asamblea.getEstado())
                    .lugar(asamblea.getLugar())
                    .horaInicio(asamblea.getHoraInicio())
                    .propietarios(propietarios)
                    .build();
            return new SuccessResult<>("Asamblea encontrada.", dto) ;
        }
        throw new ApiException("No se pudo obtener la información del registro.", HttpStatus.BAD_REQUEST);
    }
}

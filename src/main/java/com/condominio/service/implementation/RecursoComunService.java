package com.condominio.service.implementation;

import com.condominio.dto.request.RecursoComunDTO;
import com.condominio.dto.response.RecursoComunPropiDTO;
import com.condominio.dto.response.SuccessResult;
import com.condominio.persistence.model.DisponibilidadRecurso;
import com.condominio.persistence.model.RecursoComun;
import com.condominio.persistence.model.TipoRecursoComun;
import com.condominio.persistence.repository.RecursoComunRepository;
import com.condominio.service.interfaces.IRecursoComunService;
import com.condominio.util.exception.ApiException;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RecursoComunService implements IRecursoComunService {

    private final RecursoComunRepository recursoComunRepository;
    private final ModelMapper modelMapper;


    @Override
    public List<RecursoComun> findAll() {
        List<RecursoComun> recursos = new ArrayList<>();
        recursoComunRepository.findAll().forEach(recursos::add);
        if (recursos.isEmpty()) {
            throw new ApiException("No se encontraron recursos comunes .", HttpStatus.NOT_FOUND);
        }
        return recursos;
    }

    @Override
    public SuccessResult<RecursoComun> save(RecursoComunDTO recurso) {
        if(recursoComunRepository.existsByNombreIgnoreCase(recurso.getNombre())) {
            throw new ApiException("El recurso comun ya existe", HttpStatus.CONFLICT);
        }
        if (recurso.getTipoRecursoComun() == null) {
            throw new ApiException(
                    "Debe especificar un Tipo de recurso válido", HttpStatus.BAD_REQUEST);
        }
        if (recurso.getDisponibilidadRecurso() == null) {
            throw new ApiException("Debe especificar una disponibilidad válida", HttpStatus.BAD_REQUEST);
        }

        RecursoComun newRecurso = modelMapper.map(recurso, RecursoComun.class);
        recursoComunRepository.save(newRecurso);

        return new SuccessResult<>("Recurso registrado correctamente", newRecurso);
    }

    @Override
    public SuccessResult<RecursoComun> update(Long id, RecursoComunDTO recurso) {
            RecursoComun oldRecurso = recursoComunRepository.findById(id)
                    .orElseThrow(() -> new ApiException(
                            "El   recurso no existe", HttpStatus.NOT_FOUND));

        Optional<RecursoComun> recursoConMismoNombre =
                recursoComunRepository.findByNombreIgnoreCase(recurso.getNombre());

        if (recursoConMismoNombre.isPresent()
                && !recursoConMismoNombre.get().getId().equals(id)) {
            throw new ApiException(
                    "Ya existe un recurso con ese nombre",
                    HttpStatus.CONFLICT);
        }

        if (recurso.getTipoRecursoComun() == null) {
            throw new ApiException(
                    "Debe especificar un Tipo de recurso válido", HttpStatus.BAD_REQUEST);
        }

        oldRecurso.setNombre(recurso.getNombre());
        oldRecurso.setDescripcion(recurso.getDescripcion());
        oldRecurso.setTipoRecursoComun(recurso.getTipoRecursoComun());
        oldRecurso.setDisponibilidadRecurso(recurso.getDisponibilidadRecurso());

        RecursoComun actualizado = recursoComunRepository.save(oldRecurso);

        return new SuccessResult<>("Recurso modificado exitosamente", actualizado);
    }

    @Override
    public SuccessResult<RecursoComun> cambiarDisponibilidad(Long id, DisponibilidadRecurso nuevoEstado) {
        RecursoComun recurso = recursoComunRepository.findById(id)
                .orElseThrow(() -> new ApiException("El recurso no existe", HttpStatus.NOT_FOUND));

        if (recurso.getDisponibilidadRecurso() == nuevoEstado) {
            String mensajeError = switch (nuevoEstado) {
                case DISPONIBLE -> "El recurso ya está habilitado";
                case NO_DISPONIBLE -> "El recurso ya está deshabilitado";
                case EN_MANTENIMIENTO -> "El recurso ya está en mantenimiento";
            };
            throw new ApiException(mensajeError, HttpStatus.BAD_REQUEST);
        }

        recurso.setDisponibilidadRecurso(nuevoEstado);
        RecursoComun actualizado = recursoComunRepository.save(recurso);

        String mensajeExito = switch (nuevoEstado) {
            case DISPONIBLE -> "Recurso habilitado exitosamente";
            case NO_DISPONIBLE -> "Recurso deshabilitado exitosamente";
            case EN_MANTENIMIENTO -> "El recurso se ha puesto en mantenimiento exitosamente";
        };

        return new SuccessResult<>(mensajeExito, actualizado);
    }

    @Override
    public List<RecursoComun> findByTipoRecurso(TipoRecursoComun tipoRecursoComun) {
        return recursoComunRepository.findByTipoRecursoComun(tipoRecursoComun);
    }

    @Override
    public List<RecursoComunPropiDTO> findByDisponibilidad() {
        List<RecursoComun> recursos = (List<RecursoComun>) recursoComunRepository.findAll();
        if(recursos.isEmpty()){
            throw new ApiException("No hay recursos registrados", HttpStatus.BAD_REQUEST);
        }

        return recursos.stream()
                .map(recurso -> RecursoComunPropiDTO.builder()
                        .id(recurso.getId())
                        .nombre(recurso.getNombre())
                        .descripcion(recurso.getDescripcion())
                        .disponibilidadRecurso(recurso.getDisponibilidadRecurso())
                        .build())
                .toList();
    }
}

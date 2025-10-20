package com.condominio.service.implementation;

import com.condominio.dto.request.RecursoComunDTO;
import com.condominio.dto.response.SuccessResult;
import com.condominio.persistence.model.RecursoComun;
import com.condominio.persistence.model.TipoRecursoComun;
import com.condominio.persistence.repository.RecursoComunRepository;
import com.condominio.persistence.repository.TipoRecursoComunRepository;
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
    private final TipoRecursoComunRepository tipoRecursoComunRepository;


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
        if (recurso.getTipoRecursoComun() == null ||
                recurso.getTipoRecursoComun().getId() == null) {

            throw new ApiException(
                    "Debe especificar un Tipo de recurso válido", HttpStatus.BAD_REQUEST);
        }

        TipoRecursoComun tipo = tipoRecursoComunRepository.
                findById(recurso.getTipoRecursoComun().getId())
                .orElseThrow(() -> new ApiException(
                        "El Tipo de recurso no existe", HttpStatus.NOT_FOUND));

        RecursoComun newRecurso = modelMapper.map(recurso, RecursoComun.class);
        newRecurso.setTipoRecursoComun(tipo);
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
        TipoRecursoComun tipo = tipoRecursoComunRepository.findById(
                recurso.getTipoRecursoComun().getId()
        ).orElseThrow(() -> new ApiException(
                "No existe el tipo de recurso", HttpStatus.NOT_FOUND));


        oldRecurso.setNombre(recurso.getNombre());
        oldRecurso.setDescripcion(recurso.getDescripcion());
        oldRecurso.setTipoRecursoComun(tipo);

        RecursoComun actualizado = recursoComunRepository.save(oldRecurso);

        return new SuccessResult<>("Recurso modificado exitosamente", actualizado);
    }

    @Override
    public SuccessResult<RecursoComun> habilitar(Long id) {
        RecursoComun recurso = recursoComunRepository.findById(id)
                .orElseThrow(() -> new ApiException("El recurso no existe", HttpStatus.NOT_FOUND));

        if (recurso.isEstadoRecurso()) {
            throw new ApiException("El recurso ya está habilitado", HttpStatus.BAD_REQUEST);
        } else {

            recurso.setEstadoRecurso(true);
            RecursoComun actualizado = recursoComunRepository.save(recurso);

            return new SuccessResult<>("Recurso habilitado exitosamente", actualizado);
            }
    }

    @Override
    public SuccessResult<RecursoComun> deshabilitar(Long id) {
        RecursoComun recurso = recursoComunRepository.findById(id)
                .orElseThrow(() -> new ApiException("El recurso no existe", HttpStatus.NOT_FOUND));

        if (!recurso.isEstadoRecurso()) {
            throw new ApiException("El recurso ya está deshabilitado", HttpStatus.BAD_REQUEST);
        } else {

            recurso.setEstadoRecurso(false);
            RecursoComun actualizado = recursoComunRepository.save(recurso);

            return new SuccessResult<>("Recurso deshabilitado exitosamente", actualizado);
            }
    }

}

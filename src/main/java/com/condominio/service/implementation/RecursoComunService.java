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
}

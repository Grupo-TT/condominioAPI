package com.condominio.service.implementation;

import com.condominio.dto.request.TipoRecursoComunDTO;
import com.condominio.dto.response.SuccessResult;
import com.condominio.persistence.model.TipoRecursoComun;
import com.condominio.persistence.repository.TipoRecursoComunRepository;
import com.condominio.service.interfaces.ITipoRecursoComun;
import com.condominio.util.exception.ApiException;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TipoRecursoComunService implements ITipoRecursoComun {

    private final TipoRecursoComunRepository tipoRecursoComunRepository;
    private final ModelMapper modelMapper;

    public SuccessResult<TipoRecursoComunDTO> save(TipoRecursoComunDTO tipoRecurso) {
        if(tipoRecursoComunRepository.existsByNombreIgnoreCase(tipoRecurso.getNombre())){
            throw new ApiException("El Tipo de recurso comun " +
                    "ya se  encuentra registrado", HttpStatus.CONFLICT);
        }
        TipoRecursoComun newTipoRecurso = modelMapper.map(tipoRecurso, TipoRecursoComun.class);
        tipoRecursoComunRepository.save(newTipoRecurso);
        return new SuccessResult<>("Tipo de recurso registrado correctamente", tipoRecurso);
    }
    public List<TipoRecursoComun> findAll() {
        List<TipoRecursoComun> recursos = new ArrayList<>();
        tipoRecursoComunRepository.findAll().forEach(recursos::add);
        if (recursos.isEmpty()) {
            throw new ApiException("No se encontraron tipos de recursos.", HttpStatus.NOT_FOUND);
        }
        return recursos;
    }

}

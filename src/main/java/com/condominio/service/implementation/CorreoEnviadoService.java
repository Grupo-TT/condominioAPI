package com.condominio.service.implementation;

import com.condominio.dto.response.SuccessResult;
import com.condominio.persistence.model.CorreoEnviado;
import com.condominio.persistence.repository.CorreoEnviadoRepository;
import com.condominio.util.exception.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CorreoEnviadoService {

    private final CorreoEnviadoRepository correoEnviadoRepository;

    public List<CorreoEnviado> findAll() {
        List<CorreoEnviado> lista = correoEnviadoRepository.findAll();
        if (lista.isEmpty()) {
            throw new ApiException("No hay correos enviados", HttpStatus.NOT_FOUND);
        }
        return lista;
    }
    public SuccessResult<Void>delete(Long id){
         correoEnviadoRepository.findById(id)
                .orElseThrow(() -> new ApiException(
                        "No existe un comunicado con ese id",
                        HttpStatus.NOT_FOUND
                ));
        correoEnviadoRepository.deleteById(id);
        return new SuccessResult<>("Registro de  comunicado eliminado correctamente",null);
    }
}

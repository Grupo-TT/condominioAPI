package com.condominio.service.implementation;

import com.condominio.dto.request.AsistenciaDTO;
import com.condominio.dto.response.SuccessResult;
import com.condominio.persistence.model.Asamblea;
import com.condominio.persistence.model.Asistencia;
import com.condominio.persistence.repository.AsambleaRepository;
import com.condominio.persistence.repository.AsistenciaRepository;
import com.condominio.service.interfaces.IAsistenciaService;
import com.condominio.util.exception.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AsistenciaService implements IAsistenciaService {

    private final AsistenciaRepository asistenciaRepository;
    private final AsambleaRepository asambleaRepository;

    @Override
    public SuccessResult<Void> registrarAsistencia(Long idAsamblea, AsistenciaDTO asistenciaDTO) {

        Asamblea asamblea = asambleaRepository.findById(idAsamblea)
                .orElseThrow(() -> new ApiException("No existe la asamblea.", HttpStatus.BAD_REQUEST));

        Optional<Asistencia> asistenciaOptional = asistenciaRepository.findByAsambleaAndCasa_NumeroCasa(asamblea, asistenciaDTO.getNumeroCasa());
        if (asistenciaOptional.isPresent()) {
            Asistencia asistencia = asistenciaOptional.get();
            asistencia.setEstado(asistenciaDTO.isEstado());
            asistenciaRepository.save(asistencia);
        }
        return new SuccessResult<>("Asistencias registradas correctamente.", null);
    }
}

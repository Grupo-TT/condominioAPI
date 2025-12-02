package com.condominio.service.implementation;

import com.condominio.dto.request.AsistenciaDTO;
import com.condominio.dto.response.SuccessResult;
import com.condominio.persistence.model.Asamblea;
import com.condominio.persistence.model.Asistencia;
import com.condominio.persistence.model.Casa;
import com.condominio.persistence.repository.AsambleaRepository;
import com.condominio.persistence.repository.AsistenciaRepository;
import com.condominio.persistence.repository.CasaRepository;
import com.condominio.service.interfaces.IAsistenciaService;
import com.condominio.util.exception.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AsistenciaService implements IAsistenciaService {

    private final AsistenciaRepository asistenciaRepository;
    private final AsambleaRepository asambleaRepository;
    private final CasaRepository casaRepository;

    @Override
    public SuccessResult<Void> registrarAsistencia(Long idAsamblea, List<AsistenciaDTO> asistencias) {

        Asamblea asamblea = asambleaRepository.findById(idAsamblea)
                .orElseThrow(() -> new ApiException("No existe la asamblea.", HttpStatus.BAD_REQUEST));

        List<Integer> numerosDeCasas = asistencias.stream()
                .map(AsistenciaDTO::getNumeroCasa)
                .toList();

        List<Casa> casas = casaRepository.findByNumeroCasaIn(numerosDeCasas);

        Map<Integer, AsistenciaDTO> asistenciaMap = asistencias.stream()
                .collect(Collectors.toMap(AsistenciaDTO::getNumeroCasa, dto -> dto));

        for (Casa casa : casas) {
            AsistenciaDTO dto = asistenciaMap.get(casa.getNumeroCasa());

            Asistencia asistencia = new Asistencia();
            asistencia.setAsamblea(asamblea);
            asistencia.setCasa(casa);
            asistencia.setEstado(dto.isEstado());
            asistencia.setFecha(asamblea.getFecha());

            asistenciaRepository.save(asistencia);
        }
        return new SuccessResult<>("Asistencias registradas correctamente.", null);
    }
}

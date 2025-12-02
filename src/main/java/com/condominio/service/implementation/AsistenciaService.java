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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

        // Extraer números de casa desde el DTO
        List<Integer> numerosDeCasas = asistencias.stream()
                .map(AsistenciaDTO::getNumeroCasa)
                .toList();

        // Buscar casas existentes
        List<Casa> casas = casaRepository.findByNumeroCasaIn(numerosDeCasas);

        // Crear un mapa númeroCasa -> DTO para acceder rápido a estado y fecha
        Map<Integer, AsistenciaDTO> asistenciaMap = asistencias.stream()
                .collect(Collectors.toMap(AsistenciaDTO::getNumeroCasa, dto -> dto));

        // Guardar asistencia para cada casa encontrada
        for (Casa casa : casas) {
            AsistenciaDTO dto = asistenciaMap.get(casa.getNumeroCasa());

            Asistencia asistencia = new Asistencia();
            asistencia.setAsamblea(asamblea);
            asistencia.setCasa(casa);                       // tu setter era setCasas(), debería ser singular
//            asistencia.setEstado(dto.getEstado());          // ahora viene del DTO
//            asistencia.setFecha(dto.getFecha());            // también desde el DTO

            asistenciaRepository.save(asistencia);
        }

        return new SuccessResult<>("Asistencias registradas correctamente.", null);
    }
}

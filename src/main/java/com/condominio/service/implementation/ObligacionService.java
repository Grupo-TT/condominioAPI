package com.condominio.service.implementation;

import com.condominio.dto.response.EstadoCuentaDTO;
import com.condominio.dto.response.PersonaSimpleDTO;
import com.condominio.dto.response.SuccessResult;
import com.condominio.persistence.model.Casa;
import com.condominio.persistence.model.EstadoPago;
import com.condominio.persistence.model.Obligacion;
import com.condominio.persistence.model.Persona;
import com.condominio.persistence.repository.CasaRepository;
import com.condominio.persistence.repository.ObligacionRepository;
import com.condominio.persistence.repository.PersonaRepository;
import com.condominio.service.interfaces.IObligacionService;
import com.condominio.util.exception.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ObligacionService implements IObligacionService {

    private final ObligacionRepository obligacionRepository;
    private final CasaRepository casaRepository;
    private final PersonaRepository personaRepository;

    @Override
    public SuccessResult<EstadoCuentaDTO> estadoDeCuentaCasa(Long idCasa) {
        Casa casa = casaRepository.findById(idCasa).orElseThrow(() -> new ApiException(
                "No se encontró una casa con el ID " + idCasa, HttpStatus.BAD_REQUEST
        ));

        Persona propietario = personaRepository.findPropietarioByCasaId(casa.getId()).
                orElse(null);

        PersonaSimpleDTO propietarioDTO = null;
        if (propietario != null) {
            String nombreCompleto = String.format("%s %s",
                    propietario.getPrimerNombre(),
                    propietario.getPrimerApellido()
            ).trim().replaceAll(" +", " ");
            propietarioDTO = PersonaSimpleDTO.builder()
                    .nombreCompleto(nombreCompleto)
                    .correo(propietario.getUser().getEmail())
                    .telefono(propietario.getTelefono())
                    .build();
        }

        List<Obligacion> todasObligaciones = obligacionRepository.findByCasaId(idCasa);

        List<Obligacion> obligacionesPendientes = todasObligaciones.stream()
                .filter(o -> o.getEstadoPago() == EstadoPago.PENDIENTE)
                .toList();

        Long saldoPendienteTotal = obligacionesPendientes.stream()
                .mapToLong(Obligacion::getMonto)
                .sum();

        EstadoCuentaDTO dto = EstadoCuentaDTO.builder()
                .numeroCasa(casa.getNumeroCasa())
                .propietario(propietarioDTO)
                .saldoPendienteTotal(saldoPendienteTotal)
                .deudasActivas(obligacionesPendientes)
                .build();
        return new SuccessResult<>("Estado de cuenta obtenido correctamente", dto);
    }

}

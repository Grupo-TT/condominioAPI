package com.condominio.service.implementation;

import com.condominio.dto.request.MultaActualizacionDTO;
import com.condominio.dto.request.MultaRegistroDTO;
import com.condominio.dto.request.PersonaRegistroDTO;
import com.condominio.dto.response.EstadoCuentaDTO;
import com.condominio.dto.response.PersonaSimpleDTO;
import com.condominio.dto.response.SuccessResult;
import com.condominio.persistence.model.*;
import com.condominio.persistence.repository.CasaRepository;
import com.condominio.persistence.repository.ObligacionRepository;
import com.condominio.persistence.repository.PersonaRepository;
import com.condominio.service.interfaces.IObligacionService;
import com.condominio.util.events.CreatedPersonaEvent;
import com.condominio.util.exception.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
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

    @Override
    @Transactional
    public SuccessResult<Obligacion> save(MultaRegistroDTO multa) {
        Casa casa = casaRepository.findById(multa.getIdCasa())
                .orElseThrow(() -> new RuntimeException("Casa no encontrada con ID: " + multa.getIdCasa()));

        Obligacion obligacion = Obligacion.builder()
                .fechaGenerada(LocalDate.now())
                .monto(multa.getMonto())
                .motivo(multa.getMotivo())
                .casa(casa)
                .tipoObligacion(TipoObligacion.MULTA)
                .tipoPago(TipoPago.DINERO)
                .estadoPago(EstadoPago.POR_COBRAR)
                .diasGracias(0)
                .diasMaxMora(0)
                .tasaInteres(0)
                .interes(0)
                .build();

        Obligacion guardada = obligacionRepository.save(obligacion);

        return new SuccessResult<>("Multa registrada correctamente", guardada);
    }

    @Override
    public SuccessResult<Obligacion> update(Long id, MultaActualizacionDTO multa) {
        Obligacion obligacion = obligacionRepository.findById(id)
                .orElseThrow(() -> new ApiException("La multa no existe", HttpStatus.NOT_FOUND));

        Casa casa = casaRepository.findById(multa.getIdCasa())
                .orElseThrow(() -> new RuntimeException("Casa no encontrada con ID: " + multa.getIdCasa()));

        obligacion.setMonto(multa.getMonto());
        obligacion.setMotivo(multa.getMotivo());
        obligacion.setCasa(casa);

        if (multa.getTipoPago() != null) {
            obligacion.setTipoPago(multa.getTipoPago());
        }
        obligacion.setTipoObligacion(TipoObligacion.MULTA);
        Obligacion actualizada = obligacionRepository.save(obligacion);

        return new SuccessResult<>("Multa actualizada correctamente", actualizada);
    }
}
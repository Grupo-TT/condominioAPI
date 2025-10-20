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
import com.condominio.service.interfaces.IPdfService;
import com.condominio.service.interfaces.IPersonaService;
import com.condominio.util.exception.ApiException;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static com.condominio.util.constants.AppConstants.ZONE;


@Service
@RequiredArgsConstructor
public class ObligacionService implements IObligacionService {

    private final ObligacionRepository obligacionRepository;
    private final CasaRepository casaRepository;
    private final PersonaRepository personaRepository;
    private final IPersonaService personaService;
    private final IPdfService pdfService;
    private  final  EmailService emailService;
    private static final Logger log = LoggerFactory.getLogger(ObligacionService.class);

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

    public boolean estaAlDia(Long idCasa) {

        boolean tienePendientes = obligacionRepository.existsByCasaIdAndEstadoPago(idCasa, EstadoPago.PENDIENTE);
        return !tienePendientes;
    }

    public ResponseEntity<?> generarPazYSalvo(Long idCasa) {
        if (!estaAlDia(idCasa)) {
            throw new ApiException(
                    "El propietario/arrendatario tiene deudas pendientes, " +
                            "no es posible generar el paz y salvo" +
                            "", HttpStatus.BAD_REQUEST);
        }
        Persona solicitante = personaService.obtenerSolicitantePorCasa(idCasa);
        LocalDate fechaActual = LocalDate.now(ZONE);

        DateTimeFormatter formatoMostrar = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter formatoArchivo = DateTimeFormatter.ofPattern("yyyyMMdd");

        String fechaEmision = fechaActual.format(formatoMostrar);
        String fechaArchivo = fechaActual.format(formatoArchivo);

        byte[] pdfBytes;

        try {
            pdfBytes = pdfService.generarPdf(
                    solicitante.getNombreCompleto(),
                    idCasa,
                    fechaEmision
            );

        } catch (IOException e) {
            throw new ApiException("Error al generar el PDF: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        String nombreArchivo = "paz_y_salvo_" + fechaArchivo + ".pdf";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.builder("attachment")
                .filename(nombreArchivo)
                .build());

        try {
            emailService.enviarPazYSalvo(
                    solicitante.getUser().getEmail(),
                    pdfBytes,
                    nombreArchivo
            );
        } catch (MessagingException e) {
            log.error("Error al enviar correo de paz y salvo a {}: {}", solicitante.getUser().getEmail(), e.getMessage());
        }
        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }
}
package com.condominio.service.implementation;

import com.condominio.dto.request.MultaActualizacionDTO;
import com.condominio.dto.request.MultaRegistroDTO;
import com.condominio.dto.response.EstadoCuentaDTO;
import com.condominio.dto.response.PersonaSimpleDTO;
import com.condominio.dto.response.SuccessResult;
import com.condominio.persistence.model.*;
import com.condominio.persistence.repository.CasaRepository;
import com.condominio.persistence.repository.ObligacionRepository;
import com.condominio.persistence.repository.PersonaRepository;
import com.condominio.service.interfaces.IObligacionService;
import com.condominio.service.interfaces.IPagoService;
import com.condominio.service.interfaces.IPdfService;
import com.condominio.service.interfaces.IPersonaService;
import com.condominio.util.exception.ApiException;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Locale;

import static com.condominio.util.constants.AppConstants.ZONE;

@Service
@RequiredArgsConstructor
public class ObligacionService implements IObligacionService {

    private final ObligacionRepository obligacionRepository;
    private final CasaRepository casaRepository;
    private final PersonaRepository personaRepository;
    private final IPersonaService personaService;
    private final IPagoService pagoService;
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
                .ultimoPago(pagoService.obtenerFechaUltimoPagoPorCasa(idCasa).orElse(null))
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
                .titulo(multa.getTitulo())
                .motivo(multa.getMotivo())
                .casa(casa)
                .tipoObligacion(TipoObligacion.MULTA)
                .tipoPago(TipoPago.DINERO)
                .estadoPago(EstadoPago.PENDIENTE)
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
        obligacion.setTitulo(multa.getTitulo());
        obligacion.setMotivo(multa.getMotivo());
        obligacion.setCasa(casa);

        if (multa.getTipoPago() != null) {
            obligacion.setTipoPago(multa.getTipoPago());
        }
        obligacion.setTipoObligacion(TipoObligacion.MULTA);
        Obligacion actualizada = obligacionRepository.save(obligacion);

        return new SuccessResult<>("Multa actualizada correctamente", actualizada);
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

    //Prueba temporal del crear obligaciones automaticas
    private static final int MONTO_ADMIN = 200000;
    private static final int TASA_INTERES = 1;

    @Scheduled(cron = "0 0 0 1 * *", zone = "America/Bogota")
    public void generarObligacionesMensuales() {
        LocalDate hoy = LocalDate.now();
        String mes = hoy.getMonth().getDisplayName(TextStyle.FULL, Locale.of("es", "ES"));
        int anio = hoy.getYear();

        String titulo = String.format("Administración %s %d", mes, anio);
        String motivo = String.format("Cobro correspondiente a la administración de %s %d", mes, anio);

        List<Casa> casas = casaRepository.findAll();

        for (Casa casa : casas) {
            Obligacion obligacion = Obligacion.builder()
                    .fechaGenerada(hoy)
                    .fechaLimite(hoy.plusDays(10))
                    .monto(MONTO_ADMIN)
                    .tasaInteres(TASA_INTERES)
                    .motivo(motivo)
                    .titulo(titulo)
                    .tipoPago(TipoPago.DINERO)
                    .tipoObligacion(TipoObligacion.ADMINISTRACION)
                    .estadoPago(EstadoPago.PENDIENTE)
                    .casa(casa)
                    .build();

            obligacionRepository.save(obligacion);
        }
    }
}
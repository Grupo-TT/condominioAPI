package com.condominio.service.implementation;

import com.condominio.dto.response.*;
import com.condominio.persistence.model.*;
import com.condominio.persistence.repository.*;
import com.condominio.service.interfaces.IDashboardPropiService;
import com.condominio.util.exception.ApiException;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DashboardPropiService implements IDashboardPropiService {
    private final PersonaRepository personaRepository;
    private final MiembroRepository miembroRepository;
    private final MascotaRepository mascotaRepository;
    private final ObligacionRepository obligacionRepository;
    private final PagoDetalleRepository pagoDetalleRepository;
    private final SolicitudReservaRecursoRepository solicitudRepository;
    private final ModelMapper modelMapper;

    private String getEmailFromTokenOrPrincipal() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new ApiException("Usuario no autenticado", HttpStatus.UNAUTHORIZED);
        }
        Object principal = auth.getPrincipal();
        if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
            return ((org.springframework.security.core.userdetails.UserDetails) principal).getUsername();
        } else if (principal instanceof String) {
            return (String) principal;
        }
        throw new ApiException("No se pudo obtener el usuario ", HttpStatus.UNAUTHORIZED);
    }

    @Override
    public SuccessResult<InfoCasaPropiDTO> getPropiBasicInfo() {
        String email = getEmailFromTokenOrPrincipal();
        Persona propietario = personaRepository.findByUserEmail(email)
                .orElseThrow(() -> new ApiException("Propietario no encontrado", HttpStatus.NOT_FOUND));

        Casa casa = propietario.getCasa();
        if (casa == null) {
            throw new ApiException("Propietario no tiene casa asignada", HttpStatus.NOT_FOUND);
        }


        UsoCasa tipoUso = personaRepository.findArrendatarioByCasaId(casa.getId()).isPresent() ? UsoCasa.ARRENDADA : UsoCasa.RESIDENCIAL;

        int cantidadMiembros = miembroRepository.countByCasaId(casa.getId());
        int cantidadMascotas = mascotaRepository.countByCasaId(casa.getId());

        InfoCasaPropiDTO infoCasaPropiDTO = InfoCasaPropiDTO.builder()
                .numeroCasa(casa.getNumeroCasa())
                .tipoUso(tipoUso)
                .cantidadMiembros(cantidadMiembros)
                .cantidadMascotas(cantidadMascotas)
                .build();

        return new SuccessResult<>("Info del Propietario Obtenida", infoCasaPropiDTO);
    }

    @Override
    public SuccessResult<AccountStatusDTO> getAccountStatus() {
        String email = getEmailFromTokenOrPrincipal();
        Persona propietario = personaRepository.findByUserEmail(email)
                .orElseThrow(() -> new ApiException("Propietario no encontrado", HttpStatus.NOT_FOUND));

        Casa casa = propietario.getCasa();
        if (casa == null) {
            throw new ApiException("Propietario no tiene casa asignada", HttpStatus.NOT_FOUND);
        }

        // sumar valorPendiente de obligaciones de la casa
        List<Obligacion> obligaciones = obligacionRepository.findByCasaId(casa.getId());
        int saldoPendiente = obligaciones.stream()
                .mapToInt(o -> o.getValorPendiente())
                .sum();

        EstadoFinancieroCasa estadoCasa = (saldoPendiente == 0) ? EstadoFinancieroCasa.AL_DIA : EstadoFinancieroCasa.EN_MORA;

        // ultimo pago
        UltimoPagoDTO ultimoPagoDTO = null;
        Optional<PagoDetalle> lastOpt = pagoDetalleRepository.findTopByObligacionCasaIdOrderByPagoFechaPagoDesc(casa.getId());
        if (lastOpt.isPresent()) {
            PagoDetalle pd = lastOpt.get();
            LocalDate fecha = pd.getPago() != null ? pd.getPago().getFechaPago() : null;
            String concepto = pd.getObligacion() != null ? pd.getObligacion().getMotivo() : "Pago";
            int valor = pd.getMontoPagado();
            boolean fueCompleto = false;
            if (pd.getObligacion() != null) {

                int valorObligacion = pd.getObligacion().getMonto();
                fueCompleto = pd.getMontoPagado() >= valorObligacion;
            }
            ultimoPagoDTO = new UltimoPagoDTO(fecha, concepto, valor, fueCompleto);
        }

        AccountStatusDTO accountStatusDTO = AccountStatusDTO.builder()
                .saldoPendiente(saldoPendiente)
                .estadoCasa(estadoCasa)
                .ultimoPago(ultimoPagoDTO)
                .build();

        return new SuccessResult<>("Estado de la cuenta obtenido exitosamente", accountStatusDTO);
    }

    @Override
    public SuccessResult<List<SolicitudPropiDTO>> getSolicitudesPropietario() {
        String email = getEmailFromTokenOrPrincipal();
        Persona propietario = personaRepository.findByUserEmail(email)
                .orElseThrow(() -> new ApiException("Propietario no encontrado", HttpStatus.NOT_FOUND));

        Long casaId = propietario.getCasa().getId();
        List<SolicitudReservaRecurso> solicitudes = solicitudRepository.findAllByCasa_Id(casaId);

        List<SolicitudPropiDTO> dtos = solicitudes.stream()
                .map(s -> modelMapper.map(s, SolicitudPropiDTO.class))
                .toList();

        return new SuccessResult<>("Solicitudes obtenidas exitosamente", dtos);
    }
}

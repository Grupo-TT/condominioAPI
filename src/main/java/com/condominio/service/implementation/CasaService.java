package com.condominio.service.implementation;

import com.condominio.dto.response.*;
import com.condominio.persistence.model.*;
import com.condominio.persistence.repository.CasaRepository;
import com.condominio.persistence.repository.MascotaRepository;
import com.condominio.persistence.repository.ObligacionRepository;
import com.condominio.persistence.repository.PersonaRepository;
import com.condominio.service.interfaces.ICasaService;
import com.condominio.service.interfaces.IMiembroService;
import com.condominio.service.interfaces.IPagoService;
import com.condominio.util.exception.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CasaService implements ICasaService {

    private final CasaRepository casaRepository;
    private final IMiembroService miembroService;
    private final PersonaRepository personaRepository;
    private final ObligacionRepository obligacionRepository;
    private final MascotaRepository mascotaRepository;
    private final IPagoService pagoService;

    @Override
    public Optional<Casa> findById(Long id) {
        return casaRepository.findById(id);
    }


    @Override
    public void save(Casa casa) {
        casaRepository.save(casa);
    }

    @Override
    public SuccessResult<CasaCuentaDTO> estadoDeCuenta(Long idCasa) {

        List<Obligacion> todasObligaciones = obligacionRepository.findByCasaId(idCasa);

        List<Obligacion> obligacionesPendientes = todasObligaciones.stream()
                .filter(o -> o.getEstadoPago() == EstadoPago.PENDIENTE)
                .toList();

        Long saldoPendienteTotal = obligacionesPendientes.stream()
                .mapToLong(Obligacion::getMonto)
                .sum();

        CasaCuentaDTO dto = CasaCuentaDTO.builder()
                .saldoPendienteTotal(saldoPendienteTotal)
                .multasActivas(obligacionesPendientes)
                .ultimoPago(pagoService.obtenerFechaUltimoPagoPorCasa(idCasa).orElse(null))
                .build();
        return new SuccessResult<>("Estado de cuenta obtenido correctamente", dto);
    }

    public SuccessResult<List<CasaInfoDTO>> obtenerCasas() {
        List<Casa> casas = casaRepository.findAll();
        if (casas.isEmpty()) {
            throw new ApiException("No hay casas registradas " +
                    "en el sistema", HttpStatus.BAD_REQUEST);
        }
        List<CasaInfoDTO> dtos = casas.stream().map(casa -> {

            Persona propietario = personaRepository.findPropietarioByCasaId(casa.getId()).
                    orElse(null);
            int cantidadMiembros = miembroService.countByCasaId(casa.getId());
            int cantidadMascotas = mascotaRepository.sumCantidadMascotasByCasaId(casa.getId());

            PersonaSimpleDTO propietarioDTO = null;
            if (propietario != null) {
                propietarioDTO = PersonaSimpleDTO.builder()
                        .nombreCompleto(propietario.getNombreCompleto())
                        .telefono(propietario.getTelefono())
                        .correo(propietario.getUser().getEmail())
                        .build();
            }

            Map<String, Integer> mascotasMap = new LinkedHashMap<>();
            for (TipoMascota tipo : TipoMascota.values()) {
                mascotasMap.put(tipo.toString(), 0);
            }
            List<Mascota> conteos = mascotaRepository.findAllByCasa_Id(casa.getId());
            for (Mascota c : conteos) {
                mascotasMap.put(c.getTipoMascota().toString(), (int)c.getCantidad());
            }

            UsoCasa usoCasa;
            Optional<Persona> arrendatarioOpt = personaRepository.findArrendatarioByCasaId(casa.getId());
            if (arrendatarioOpt.isPresent()) {
                usoCasa = UsoCasa.ARRENDADA;

            }else{
                usoCasa = UsoCasa.RESIDENCIAL;
            }
            EstadoFinancieroCasa estadoFinancieroCasa;
            if(obligacionRepository.existsByCasaIdAndEstadoPago(casa.getId(),EstadoPago.PENDIENTE)){

                estadoFinancieroCasa= EstadoFinancieroCasa.EN_MORA;

            }else{
                estadoFinancieroCasa= EstadoFinancieroCasa.AL_DIA;
            }
            CasaInfoDTO dto = new CasaInfoDTO();
            dto.setNumeroCasa(casa.getNumeroCasa());
            dto.setPropietario(propietarioDTO);
            dto.setCantidadMiembros(cantidadMiembros);
            dto.setCantidadMascotas(cantidadMascotas);
            dto.setMascotas(mascotasMap);
            dto.setUsoCasa(usoCasa);
            dto.setEstadoFinancieroCasa(estadoFinancieroCasa);

            return dto;
        }).toList();
        return new SuccessResult<>("Casas obtenidas correctamente", dtos);
    }

    public SuccessResult<List<CasaDeudoraDTO>> obtenerCasasConObligacionesPorCobrar() {
        List<Casa> casas = casaRepository.findCasasConObligacionesPorCobrar();
        if (casas.isEmpty()) {
            throw new ApiException("No hay casas con obligaciones por cobrar", HttpStatus.BAD_REQUEST);
        }

        List<CasaDeudoraDTO> dtos = casas.stream().map(casa -> {
            Persona propietario = personaRepository.findPropietarioByCasaId(casa.getId())
                    .orElse(null);

            PersonaSimpleDTO propietarioDTO = null;
            if (propietario != null) {
                propietarioDTO = PersonaSimpleDTO.builder()
                        .nombreCompleto(propietario.getNombreCompleto())
                        .telefono(propietario.getTelefono())
                        .correo(propietario.getUser().getEmail())
                        .build();
            }

            List<Obligacion> pendientes = obligacionRepository
                    .findByCasaIdAndEstadoPagoIsNotOrderByFechaGeneradaDesc(casa.getId(), EstadoPago.CONDONADO);

            int saldoPendiente = pendientes.stream()
                    .mapToInt(Obligacion::getValorPendiente)
                    .sum();

            List<MostrarObligacionDTO> obligacionesDTO = pendientes.stream()
                    .map(o -> MostrarObligacionDTO.builder()
                            .id((o.getId()))
                            .estado(o.getEstadoPago().name())
                            .motivo(o.getMotivo())
                            .casa(o.getCasa().getNumeroCasa())
                            .monto(o.getMonto())
                            .valorTotal(o.getValorTotal())
                            .valorPendiente(o.getValorPendiente())
                            .estadoPago(o.getEstadoPago())
                            .montoPagado(o.getMontoPagado())
                            .tipoObligacion(o.getTipoObligacion())
                            .build())
                    .toList();

            CasaDeudoraDTO dto = new CasaDeudoraDTO();
            dto.setNumeroCasa(casa.getNumeroCasa());
            dto.setPropietario(propietarioDTO);
            dto.setSaldoPendiente(saldoPendiente);
            dto.setObligacionesPendientes(obligacionesDTO);
            dto.setUltimoPago(pagoService.obtenerFechaUltimoPagoPorCasa(casa.getId()).orElse(null));
            return dto;
        }).toList();

        return new SuccessResult<>("Casas con obligaciones por cobrar obtenidas correctamente", dtos);
    }

    public SuccessResult<List<CasaDeudoraDTO>>  obtenerObligacionesPorCasa() {
        List<Casa> casas = casaRepository.findAll();
        if (casas.isEmpty()) {
            throw new ApiException("No hay casas con obligaciones", HttpStatus.BAD_REQUEST);
        }

        List<CasaDeudoraDTO> dtos = casas.stream().map(casa -> {
            Persona propietario = personaRepository.findPropietarioByCasaId(casa.getId())
                    .orElse(null);

            PersonaSimpleDTO propietarioDTO = null;
            if (propietario != null) {
                propietarioDTO = PersonaSimpleDTO.builder()
                        .nombreCompleto(propietario.getNombreCompleto())
                        .telefono(propietario.getTelefono())
                        .correo(propietario.getUser().getEmail())
                        .build();
            }

            List<Obligacion> obligaciones = obligacionRepository
                    .findByCasaIdAndEstadoPagoIsNotOrderByFechaGeneradaDesc(casa.getId(), EstadoPago.CONDONADO);

            int saldoPendiente = obligaciones.stream()
                    .mapToInt(Obligacion::getValorPendiente)
                    .sum();

            int interes = obligaciones.stream()
                    .mapToInt(Obligacion::getInteres)
                    .sum();

            List<MostrarObligacionDTO> obligacionesDTO = obligaciones.stream()
                    .map(o -> MostrarObligacionDTO.builder()
                            .id((o.getId()))
                            .estado(o.getEstadoPago().name())
                            .titulo(o.getTitulo())
                            .casa(o.getCasa().getNumeroCasa())
                            .monto(o.getMonto())
                            .valorTotal(o.getValorTotal())
                            .valorPendiente(o.getValorPendiente())
                            .estadoPago(o.getEstadoPago())
                            .montoPagado(o.getMontoPagado())
                            .tipoObligacion(o.getTipoObligacion())
                            .interes(o.getInteres())
                            .build())
                    .toList();

            CasaDeudoraDTO dto = new CasaDeudoraDTO();
            dto.setNumeroCasa(casa.getNumeroCasa());
            dto.setPropietario(propietarioDTO);
            dto.setSaldoPendiente(saldoPendiente);
            dto.setObligacionesPendientes(obligacionesDTO);
            dto.setUltimoPago(pagoService.obtenerFechaUltimoPagoPorCasa(casa.getId()).orElse(null));
            dto.setInteres(interes);
            return dto;
        }).toList();

        return new SuccessResult<>("Casas con obligaciones por cobrar obtenidas correctamente", dtos);
    }

}



package com.condominio.service.implementation;

import com.condominio.dto.response.CasaCuentaDTO;
import com.condominio.dto.response.CasaInfoDTO;
import com.condominio.dto.response.SuccessResult;
import com.condominio.persistence.model.Casa;
import com.condominio.persistence.model.EstadoPago;
import com.condominio.persistence.model.Obligacion;
import com.condominio.persistence.model.Persona;
import com.condominio.persistence.repository.CasaRepository;
import com.condominio.persistence.repository.ObligacionRepository;
import com.condominio.persistence.repository.PersonaRepository;
import com.condominio.service.interfaces.ICasaService;
import com.condominio.service.interfaces.IMascotaService;
import com.condominio.service.interfaces.IMiembroService;
import com.condominio.util.exception.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CasaService implements ICasaService {

    private final CasaRepository casaRepository;
    private final IMiembroService miembroService;
    private final IMascotaService mascotaService;
    private final PersonaRepository personaRepository;
    private final ObligacionRepository obligacionRepository;



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
                .build();
        return new SuccessResult<>("Estado de cuenta obtenido correctamente",dto);
    }

    public SuccessResult<List<CasaInfoDTO>> obtenerCasas(){
        List<Casa> casas = casaRepository.findAll();
        if(casas.isEmpty()){
            throw new ApiException("No hay casas registradas " +
                    "en el sistema", HttpStatus.BAD_REQUEST);
        }
        List<CasaInfoDTO> dtos =casas.stream().map(casa -> {

            Persona propietario = personaRepository.findPropietarioByCasaId(casa.getId()).
                    orElse(null);
            int cantidadMiembros = miembroService.countByCasaId(casa.getId());
            int cantidadMascotas = mascotaService.countByCasaId(casa.getId());


            CasaInfoDTO dto = new CasaInfoDTO();
            dto.setNumeroCasa(casa.getNumeroCasa());
            dto.setPropietario(propietario);
            dto.setCantidadMiembros(cantidadMiembros);
            dto.setCantidadMascotas(cantidadMascotas);

            return dto;
        }).toList();
        return new SuccessResult<>("Casas obtenidas correctamente", dtos);
    }



}



package com.condominio.service.implementation;

import com.condominio.dto.request.MascotaDTO;
import com.condominio.dto.response.SuccessResult;
import com.condominio.persistence.model.Casa;
import com.condominio.persistence.model.Mascota;
import com.condominio.persistence.repository.CasaRepository;
import com.condominio.persistence.repository.MascotaRepository;
import com.condominio.service.interfaces.IMascotaService;
import com.condominio.util.exception.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MascotaService implements IMascotaService {

    private final MascotaRepository mascotaRepository;
    private final CasaRepository casaRepository;
    @Override
    public int countByCasaId(Long idCasa) {
        return mascotaRepository.countByCasaId(idCasa);
    }

    @Override
    public SuccessResult<Void> addMascota(MascotaDTO mascotaDTO) {
        Optional<Mascota> mascotaOptional = mascotaRepository.findByTipoMascotaAndCasa_Id(mascotaDTO.getTipoMascota(), mascotaDTO.getIdCasa());
        if (mascotaOptional.isPresent()) {
            Mascota mascota = mascotaOptional.get();
            mascota.addCantidad(mascotaDTO.getCantidad());
            mascotaRepository.save(mascota);
            return new SuccessResult<>("Mascota creada satisfactoriamente", null);
        }else {
            Optional<Casa> casa = casaRepository.findById(mascotaDTO.getIdCasa());
            if (!casa.isPresent()) {
                throw new ApiException("No se encontró la casa.", HttpStatus.NOT_FOUND);
            }
            Mascota newMascota = Mascota.builder()
                    .tipoMascota(mascotaDTO.getTipoMascota())
                    .cantidad(mascotaDTO.getCantidad())
                    .casa(casa.get())
                    .build();
            mascotaRepository.save(newMascota);
            return new SuccessResult<>("Mascota creada satisfactoriamente", null);
        }
    }

    @Override
    public SuccessResult<Void> subtractMascota(MascotaDTO mascotaDTO) {
        if (mascotaDTO.getCantidad() < 0) {
            throw new ApiException("No puede ingresar un número menor que 0.", HttpStatus.BAD_REQUEST);
        }
        Optional<Mascota> mascotaOptional = mascotaRepository.findByTipoMascotaAndCasa_Id(mascotaDTO.getTipoMascota(), mascotaDTO.getIdCasa());
        if (mascotaOptional.isPresent()) {
            Mascota mascota = mascotaOptional.get();
            mascota.setCantidad(mascotaDTO.getCantidad());
            mascotaRepository.save(mascota);
            return new SuccessResult<>("Se modificó la cantidad satisfactoriamente", null);
        }else {
            throw new ApiException("No tiene mascotas para editar.", HttpStatus.OK);
        }
    }

    @Override
    public SuccessResult<List<MascotaDTO>> findMascotasByCasa(Long idCasa) {
        List<Mascota> mascotasCasa = mascotaRepository.findAllByCasa_Id(idCasa);
        if (mascotasCasa.isEmpty()) {
            throw new ApiException("No tiene mascotas registradas.", HttpStatus.OK);
        }
        List<MascotaDTO> mascotasDTO = new ArrayList<>();
        for(Mascota mascota : mascotasCasa) {
            MascotaDTO mascotaDTO = new MascotaDTO();
            mascotaDTO.setIdCasa(mascota.getCasa().getId());
            mascotaDTO.setCantidad(mascota.getCantidad());
            mascotaDTO.setTipoMascota(mascota.getTipoMascota());
            mascotasDTO.add(mascotaDTO);
        }
        return new SuccessResult<>("Sus mascotas", mascotasDTO);
    }
}

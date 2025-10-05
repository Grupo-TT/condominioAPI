package com.condominio.service.implementation;

import com.condominio.persistence.repository.MascotaRepository;
import com.condominio.service.interfaces.IMascotaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MascotaService implements IMascotaService {

    private final MascotaRepository mascotaRepository;
    @Override
    public int countByCasaId(Long IdCasa) {
        return mascotaRepository.countByCasaId(IdCasa);
    }
}

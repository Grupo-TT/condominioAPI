package com.condominio.service.implementation;

import com.condominio.persistence.repository.MiembroRepository;
import com.condominio.service.interfaces.IMiembroService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MiembroService implements IMiembroService {

    private final MiembroRepository miembroRepository;
    @Override
    public int countByCasaId(Long IdCasa) {
        return miembroRepository.countByCasaId(IdCasa);
    }
}

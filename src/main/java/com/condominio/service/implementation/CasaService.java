package com.condominio.service.implementation;

import com.condominio.persistence.model.Casa;
import com.condominio.persistence.repository.CasaRepository;
import com.condominio.service.interfaces.ICasaService;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CasaService implements ICasaService {

    private final CasaRepository casaRepository;

    public CasaService(CasaRepository casaRepository) {
        this.casaRepository = casaRepository;
    }

    @Override
    public Optional<Casa> findById(Long id) {
        return casaRepository.findById(id);
    }

    @Override
    public void save(Casa casa) {
        casaRepository.save(casa);
    }

}

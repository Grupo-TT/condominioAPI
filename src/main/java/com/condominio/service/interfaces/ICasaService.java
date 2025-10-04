package com.condominio.service.interfaces;


import com.condominio.persistence.model.Casa;
import java.util.Optional;

public interface ICasaService {
    Optional<Casa> findById(Long id);
    void save(Casa casa);



}

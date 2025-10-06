package com.condominio.persistence.repository;

import com.condominio.persistence.model.Mascota;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MascotaRepository extends CrudRepository<Mascota, Long> {
    int countByCasaId(Long IdCasa);
}

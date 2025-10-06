package com.condominio.persistence.repository;

import com.condominio.persistence.model.Miembro;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MiembroRepository extends CrudRepository<Miembro, Long> {
    int countByCasaId(Long IdCasa);
    List<Miembro> findByCasaIdAndEstadoTrue(Long casaId);
}

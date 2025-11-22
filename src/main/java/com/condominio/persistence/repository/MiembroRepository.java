package com.condominio.persistence.repository;

import com.condominio.persistence.model.Casa;
import com.condominio.persistence.model.Miembro;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MiembroRepository extends CrudRepository<Miembro, Long> {
    int countByCasaId(Long idCasa);
    List<Miembro> findByCasaIdAndEstadoTrue(Long casaId);
    boolean existsByNumeroDocumento(Long numeroDocumento);
    List<Miembro> findByCasaId(Long casaId);
    boolean existsByNumeroDocumentoAndIdNot(Long numeroDocumento, Long id);
    void deleteAllByCasa(Casa casa);
}

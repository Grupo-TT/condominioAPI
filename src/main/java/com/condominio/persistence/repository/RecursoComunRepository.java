package com.condominio.persistence.repository;

import com.condominio.persistence.model.RecursoComun;
import com.condominio.persistence.model.TipoRecursoComun;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RecursoComunRepository extends CrudRepository<RecursoComun, Long> {

    boolean existsByNombreIgnoreCase(String nombre);
    Optional<RecursoComun> findByNombreIgnoreCase(String nombre);
    List<RecursoComun> findByTipoRecursoComun(TipoRecursoComun tipoRecursoComun);
}

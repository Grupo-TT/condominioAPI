package com.condominio.persistence.repository;

import com.condominio.persistence.model.TipoRecursoComun;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TipoRecursoComunRepository extends CrudRepository<TipoRecursoComun, Long> {
}

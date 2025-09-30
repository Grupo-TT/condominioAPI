package com.condominio.persistence.repository;

import com.condominio.persistence.model.RecursoComun;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RecursoComunRepository extends CrudRepository<RecursoComun, Long> {
}

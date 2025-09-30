package com.condominio.persistence.repository;

import com.condominio.persistence.model.Obligacion;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ObligacionRepository extends CrudRepository<Obligacion, Long> {
}

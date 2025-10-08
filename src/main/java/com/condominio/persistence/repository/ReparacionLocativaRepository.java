package com.condominio.persistence.repository;

import com.condominio.persistence.model.ReparacionLocativa;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReparacionLocativaRepository extends CrudRepository<ReparacionLocativa, Long> {
}

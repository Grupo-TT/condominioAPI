package com.condominio.persistence.repository;

import com.condominio.persistence.model.Propietario;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PropietarioRepository extends CrudRepository<Propietario, Long> {
}

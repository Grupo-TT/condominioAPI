package com.condominio.persistence.repository;

import com.condominio.persistence.model.Asistencia;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AsistenciaRepository extends CrudRepository<Asistencia, Long> {
}

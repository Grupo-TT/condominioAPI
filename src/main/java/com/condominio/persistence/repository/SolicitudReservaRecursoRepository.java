package com.condominio.persistence.repository;

import com.condominio.persistence.model.SolicitudReservaRecurso;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SolicitudReservaRecursoRepository extends CrudRepository<SolicitudReservaRecurso, Long> {
}

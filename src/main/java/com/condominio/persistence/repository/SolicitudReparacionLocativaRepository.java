package com.condominio.persistence.repository;

import com.condominio.persistence.model.SolicitudReparacionLocativa;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SolicitudReparacionLocativaRepository extends CrudRepository<SolicitudReparacionLocativa, Long> {
}

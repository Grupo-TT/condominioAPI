package com.condominio.persistence.repository;

import com.condominio.persistence.model.EstadoSolicitud;
import com.condominio.persistence.model.ReparacionLocativa;
import com.condominio.persistence.model.SolicitudReparacionLocativa;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SolicitudReparacionLocativaRepository extends CrudRepository<SolicitudReparacionLocativa, Long> {
    List<SolicitudReparacionLocativa> findByEstadoSolicitud(EstadoSolicitud estadoSolicitud);
}

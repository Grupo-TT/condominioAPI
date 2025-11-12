package com.condominio.persistence.repository;

import com.condominio.persistence.model.Casa;
import com.condominio.persistence.model.EstadoSolicitud;
import com.condominio.persistence.model.RecursoComun;
import com.condominio.persistence.model.SolicitudReservaRecurso;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface SolicitudReservaRecursoRepository extends CrudRepository<SolicitudReservaRecurso, Long> {

    List<SolicitudReservaRecurso> findByEstadoSolicitud(EstadoSolicitud estadoSolicitud);
    List<SolicitudReservaRecurso> findByRecursoComunAndFechaSolicitud(RecursoComun recursoComun, LocalDate fecha);
    List<SolicitudReservaRecurso> findAllByCasa(Casa casa);

}

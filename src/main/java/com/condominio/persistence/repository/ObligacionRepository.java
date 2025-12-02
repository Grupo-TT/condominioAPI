package com.condominio.persistence.repository;

import com.condominio.persistence.model.EstadoPago;
import com.condominio.persistence.model.Obligacion;
import com.condominio.persistence.model.TipoObligacion;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ObligacionRepository extends CrudRepository<Obligacion, Long> {

    List<Obligacion> findByCasaId(Long casaId);
    List<Obligacion> findByCasaIdAndEstadoPagoIsNotOrderByFechaGeneradaDesc(Long id, EstadoPago estadoPago);
    boolean existsByCasaIdAndEstadoPago(Long casaId, EstadoPago estadoPago);
    List<Obligacion> findByTipoObligacionOrderByFechaGeneradaDesc(TipoObligacion tipoObligacion);
    List<Obligacion> findByCasaIdOrderByFechaGeneradaDesc(Long casaId);
}

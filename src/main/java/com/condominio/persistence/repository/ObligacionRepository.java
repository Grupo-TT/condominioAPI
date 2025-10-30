package com.condominio.persistence.repository;

import com.condominio.persistence.model.EstadoPago;
import com.condominio.persistence.model.Obligacion;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ObligacionRepository extends CrudRepository<Obligacion, Long> {

    List<Obligacion> findByCasaId(Long casaId);

    List<Obligacion> findByCasaIdAndEstadoPagoIsNot(Long id, EstadoPago estadoPago);

    boolean existsByCasaIdAndEstadoPago(Long casaId, EstadoPago estadoPago);
}

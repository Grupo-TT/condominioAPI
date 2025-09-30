package com.condominio.persistence.repository;

import com.condominio.persistence.model.PagoDetalle;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PagoDetalleRepository extends CrudRepository<PagoDetalle, Long> {
}

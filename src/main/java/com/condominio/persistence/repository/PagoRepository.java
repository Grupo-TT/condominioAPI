package com.condominio.persistence.repository;

import com.condominio.persistence.model.Pago;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PagoRepository extends CrudRepository<Pago, Long> {
}

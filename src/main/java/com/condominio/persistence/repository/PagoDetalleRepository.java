package com.condominio.persistence.repository;

import com.condominio.persistence.model.PagoDetalle;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface PagoDetalleRepository extends CrudRepository<PagoDetalle, Long> {
    @Query("""
           SELECT MAX(pd.pago.fechaPago)
           FROM PagoDetalle pd
           WHERE pd.obligacion.casa.id = :idCasa
           """)
    Optional<LocalDate> findFechaUltimoPagoByCasaId(@Param("idCasa") Long idCasa);
}

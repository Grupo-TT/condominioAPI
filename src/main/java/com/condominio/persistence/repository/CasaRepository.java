package com.condominio.persistence.repository;

import com.condominio.persistence.model.Casa;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CasaRepository extends CrudRepository<Casa, Long> {

    List<Casa> findAll();
    @Query("""
           SELECT DISTINCT o.casa
           FROM Obligacion o
           WHERE o.estadoPago = 'PENDIENTE'
           """)
    List<Casa> findCasasConObligacionesPorCobrar();
}

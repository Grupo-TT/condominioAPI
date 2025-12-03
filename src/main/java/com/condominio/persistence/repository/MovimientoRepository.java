package com.condominio.persistence.repository;

import com.condominio.persistence.model.Movimiento;
import com.condominio.persistence.model.TipoMovimiento;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface MovimientoRepository extends CrudRepository<Movimiento, Long> {

    List<Movimiento> findByFechaMovimientoBetweenOrderByFechaMovimientoDesc(LocalDate desde, LocalDate hasta);

    @Query("select coalesce(sum(m.monto),0) from movimientos m where m.tipoMovimiento = :tipo and m.fechaMovimiento between :desde and :hasta")
    int sumMontoByTipoBetween(@Param("tipo") TipoMovimiento tipo,
                               @Param("desde") LocalDate desde,
                               @Param("hasta") LocalDate hasta);

    @Query("select coalesce(sum(m.monto),0) from movimientos m where m.fechaMovimiento <= :hasta")
    int sumMontoUntil(@Param("hasta") LocalDate hasta);

}

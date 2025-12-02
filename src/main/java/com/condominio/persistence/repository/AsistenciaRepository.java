package com.condominio.persistence.repository;

import com.condominio.persistence.model.Asamblea;
import com.condominio.persistence.model.Asistencia;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AsistenciaRepository extends CrudRepository<Asistencia, Long> {
    List<Asistencia> findAllByAsamblea(Asamblea asamblea);
    Optional<Asistencia> findByAsambleaAndCasa_NumeroCasa(Asamblea asamblea, int numeroCasa);
}

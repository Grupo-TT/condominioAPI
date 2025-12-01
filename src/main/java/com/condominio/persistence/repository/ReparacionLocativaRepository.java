package com.condominio.persistence.repository;

import com.condominio.persistence.model.ReparacionLocativa;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReparacionLocativaRepository extends CrudRepository<ReparacionLocativa, Long> {
    List<ReparacionLocativa> findAll();
}

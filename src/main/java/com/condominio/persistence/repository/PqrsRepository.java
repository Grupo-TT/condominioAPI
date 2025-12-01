package com.condominio.persistence.repository;

import com.condominio.persistence.model.PqrsEntity;
import com.condominio.persistence.model.EstadoPqrs;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PqrsRepository extends CrudRepository<PqrsEntity, Long> {
    List<PqrsEntity> findByEstadoPqrs(EstadoPqrs estadoPqrs);
}

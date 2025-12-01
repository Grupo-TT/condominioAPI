package com.condominio.persistence.repository;

import com.condominio.persistence.model.CargoAdministracion;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CargoAdministracionRepository extends CrudRepository<CargoAdministracion, Long> {
}

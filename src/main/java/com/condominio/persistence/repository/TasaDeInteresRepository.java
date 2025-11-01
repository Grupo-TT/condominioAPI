package com.condominio.persistence.repository;

import com.condominio.persistence.model.TasaDeInteres;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TasaDeInteresRepository  extends CrudRepository<TasaDeInteres,Long> {

}

package com.condominio.persistence.repository;

import com.condominio.persistence.model.Casa;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CasaRepository extends CrudRepository<Casa, Long> {

}

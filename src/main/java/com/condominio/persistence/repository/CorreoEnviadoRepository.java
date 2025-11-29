package com.condominio.persistence.repository;

import com.condominio.persistence.model.CorreoEnviado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CorreoEnviadoRepository extends JpaRepository<CorreoEnviado, Long> {
}

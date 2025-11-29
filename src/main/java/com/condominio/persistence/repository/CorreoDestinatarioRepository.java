package com.condominio.persistence.repository;

import com.condominio.persistence.model.CorreoDestinatario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface CorreoDestinatarioRepository extends JpaRepository<CorreoDestinatario, Long> {

    List<CorreoDestinatario> findByCorreoEnviadoId(Long correoId);
}

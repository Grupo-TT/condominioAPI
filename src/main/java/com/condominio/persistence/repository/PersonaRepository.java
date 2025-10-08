package com.condominio.persistence.repository;

import com.condominio.persistence.model.Persona;
import com.condominio.persistence.model.RoleEnum;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface PersonaRepository extends CrudRepository<Persona, Long> {

    Optional<Persona> findByNumeroDocumento(Long numeroDocumento);

    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END " +
            "FROM Persona p JOIN p.user u JOIN u.roles r " +
            "WHERE p.casa.id = :casaId AND r.roleEnum = :roleEnum")
    boolean existsRoleInCasa(@Param("casaId") Long casaId, @Param("roleEnum") RoleEnum roleEnum);

    @Query("SELECT p FROM Persona p " +
            "JOIN p.user u " +
            "JOIN u.roles r " +
            "WHERE p.casa.id = :casaId AND r.roleEnum = 'PROPIETARIO'")
    Optional<Persona> findPropietarioByCasaId(@Param("casaId") Long casaId);

    @Query("SELECT p FROM Persona p " +
            "JOIN p.user u " +
            "JOIN u.roles r " +
            "WHERE p.casa.id = :casaId AND r.roleEnum = 'ARRENDATARIO'")
    Optional<Persona> findArrendatarioByCasaId(@Param("casaId") Long casaId);
}



package com.condominio.persistence.repository;

import com.condominio.dto.response.MascotaCountDTO;
import com.condominio.persistence.model.Casa;
import com.condominio.persistence.model.Mascota;
import com.condominio.persistence.model.TipoMascota;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MascotaRepository extends CrudRepository<Mascota, Long> {
    int countByCasaId(Long idCasa);

    @Query("SELECT new com.condominio.dto.response.MascotaCountDTO(m.tipoMascota, COUNT(m)) " +
            "FROM Mascota m " +
            "WHERE m.casa.id = :casaId " +
            "GROUP BY m.tipoMascota")
    List<MascotaCountDTO> contarPorTipo(@Param("casaId") Long casaId);
    void deleteAllByCasa(Casa casa);
    Optional<Mascota> findByTipoMascotaAndCasa_Id(TipoMascota tipoMascota, Long casaId);
    List<Mascota> findAllByCasa_Id(Long casaId);
}

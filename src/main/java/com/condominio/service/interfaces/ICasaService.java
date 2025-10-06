package com.condominio.service.interfaces;



import com.condominio.dto.response.CasaCuentaDTO;
import com.condominio.dto.response.CasaInfoDTO;
import com.condominio.dto.response.SuccessResult;
import com.condominio.persistence.model.Casa;
import java.util.List;
import java.util.Optional;

public interface ICasaService {
    Optional<Casa> findById(Long id);

    void save(Casa casa);
    SuccessResult<CasaCuentaDTO> EstadoDeCuenta(Long idCasa);
    SuccessResult<List<CasaInfoDTO>> obtenerCasas();



}

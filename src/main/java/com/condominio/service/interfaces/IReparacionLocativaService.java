package com.condominio.service.interfaces;

import com.condominio.dto.response.ReparacionLocativaDTO;

import java.util.List;

public interface IReparacionLocativaService {
    List<ReparacionLocativaDTO> findAll();
}

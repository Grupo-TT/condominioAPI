package com.condominio.service.interfaces;

import com.condominio.dto.response.ReservaDTO;
import java.util.List;

public interface IReservaService {
    List<ReservaDTO> findAllProximas();
}

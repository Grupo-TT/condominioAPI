package com.condominio.service.implementation;

import com.condominio.dto.response.ConfiguracionFinancieraDTO;
import com.condominio.persistence.model.CargoAdministracion;
import com.condominio.persistence.model.PagoAdicional;
import com.condominio.persistence.model.TasaDeInteres;
import com.condominio.persistence.repository.CargoAdministracionRepository;
import com.condominio.persistence.repository.PagoAdicionalRepository;
import com.condominio.persistence.repository.TasaDeInteresRepository;
import com.condominio.util.exception.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ConfiguracionFinancieraService {

    private final PagoAdicionalRepository pagoAdicionalRepository;
    private final TasaDeInteresRepository tasaDeInteresRepository;
    private final CargoAdministracionRepository cargoAdministracionRepository;

    public ConfiguracionFinancieraDTO obtenerConfiguracion() {
        PagoAdicional pago = pagoAdicionalRepository.findById(1L)
                .orElseThrow(() -> new ApiException("No existe pago adicional", HttpStatus.NOT_FOUND));

        TasaDeInteres tasa = tasaDeInteresRepository.findById(1L)
                .orElseThrow(() -> new ApiException("No existe tasa de interes", HttpStatus.NOT_FOUND));

        CargoAdministracion cargo = cargoAdministracionRepository.findById(1L)
                .orElseThrow(() -> new ApiException("No existe cargo de administración", HttpStatus.NOT_FOUND));

        return new ConfiguracionFinancieraDTO(pago, tasa, cargo);
    }
}

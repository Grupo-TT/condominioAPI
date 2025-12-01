package com.condominio.dto.response;

import com.condominio.persistence.model.CargoAdministracion;
import com.condominio.persistence.model.PagoAdicional;
import com.condominio.persistence.model.TasaDeInteres;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ConfiguracionFinancieraDTO {
    private PagoAdicional pagoAdicional;
    private TasaDeInteres tasaDeInteres;
    private CargoAdministracion cargoAdministracion;
}

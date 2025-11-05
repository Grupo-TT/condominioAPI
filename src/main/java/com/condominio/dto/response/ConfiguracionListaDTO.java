package com.condominio.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConfiguracionListaDTO {
    private List<ConfigItemDTO> configuraciones;
}


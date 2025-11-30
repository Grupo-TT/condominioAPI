package com.condominio.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DestinatarioInfoDTO {
    private String nombreCompleto;
    private Long  idCasa;
    private String email;
}

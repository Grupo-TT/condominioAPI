package com.condominio.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class MiembrosResponseDTO {

    List<MiembrosDTO> miembros;
    boolean arrendatarioExiste;
    boolean miembrosExisten;

}

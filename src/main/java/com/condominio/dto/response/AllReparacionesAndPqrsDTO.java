package com.condominio.dto.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AllReparacionesAndPqrsDTO {
    private List<SolicitudReparacionLocativaDTO> reparaciones;
    private List<PqrsDTO> listaPqrs;
}

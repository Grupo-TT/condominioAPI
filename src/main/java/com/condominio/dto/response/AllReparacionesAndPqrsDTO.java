package com.condominio.dto.response;

import com.condominio.persistence.model.PqrsEntity;
import com.condominio.persistence.model.ReparacionLocativa;
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

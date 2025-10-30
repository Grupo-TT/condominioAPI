package com.condominio.dto.response;

import com.condominio.persistence.model.DisponibilidadRecurso;
import lombok.*;
import lombok.Builder;


@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecursoComunPropiDTO {
    private long id;
    private String nombre;
    private String descripcion;
    private DisponibilidadRecurso disponibilidadRecurso;
}

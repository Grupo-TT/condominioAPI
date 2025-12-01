package com.condominio.dto.response;

import com.condominio.persistence.model.TipoPqrs;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PqrsPropiDTO {
    private Long id;
    private String titulo;
    private String descripcion;
    private LocalDate fechaRealizacion;
    private TipoPqrs tipoPqrs;
}

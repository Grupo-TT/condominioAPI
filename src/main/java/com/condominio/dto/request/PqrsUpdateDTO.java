package com.condominio.dto.request;

import com.condominio.persistence.model.EstadoPqrs;
import com.condominio.persistence.model.TipoPqrs;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PqrsUpdateDTO {
    private Long id;
    private String titulo;
    private String descripcion;
    private EstadoPqrs estadoPqrs;
    private LocalDate fechaRealizacion;
    private TipoPqrs tipoPqrs;
}

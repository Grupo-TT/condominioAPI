package com.condominio.dto.response;

import com.condominio.persistence.model.Casa;
import com.condominio.persistence.model.EstadoPqrs;
import com.condominio.persistence.model.TipoPqrs;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PqrsDTO {
    private Long id;
    private String titulo;
    private String descripcion;
    private LocalDate fechaRealizacion;
    private Casa casa;
    private TipoPqrs tipoPqrs;
    private EstadoPqrs estadoPqrs;
    private PersonaSimpleDTO solicitante;
}

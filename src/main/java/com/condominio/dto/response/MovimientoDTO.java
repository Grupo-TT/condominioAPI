package com.condominio.dto.response;

import com.condominio.persistence.model.CategoriaMovimiento;
import com.condominio.persistence.model.TipoMovimiento;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovimientoDTO {

    private long id;
    private String descripcion;
    private int monto;
    private LocalDate fecha;
    private TipoMovimiento tipo;
    private CategoriaMovimiento categoria;
    private String responsable;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}

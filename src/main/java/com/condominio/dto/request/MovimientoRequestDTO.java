package com.condominio.dto.request;

import com.condominio.persistence.model.CategoriaMovimiento;
import com.condominio.persistence.model.TipoMovimiento;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MovimientoRequestDTO {
    @NotNull
    private LocalDate fecha;

    @NotNull
    private TipoMovimiento tipo;

    @NotNull
    private String concepto;

    private String descripcion;

    @NotNull
    @Positive(message = "monto debe ser mayor que 0")
    private int monto;

    @NotNull
    private CategoriaMovimiento categoria;


    private String responsable;
}

package com.condominio.dto.request;

import com.condominio.persistence.model.EstadoAsamblea;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import java.time.LocalTime;
import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class AsambleaDTO {

    @NotBlank(message = "Por favor, ingresa un titulo para la asamblea.")
    private String titulo;
    private String descripcion;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "America/Bogota")
    private Date fecha;
    private EstadoAsamblea estado;
    private String lugar;
    private LocalTime horaInicio;
}

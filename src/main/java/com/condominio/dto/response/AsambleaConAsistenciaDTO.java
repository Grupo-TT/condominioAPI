package com.condominio.dto.response;

import com.condominio.persistence.model.Asistencia;
import com.condominio.persistence.model.EstadoAsamblea;
import lombok.Builder;
import lombok.Data;

import java.time.LocalTime;
import java.util.Date;
import java.util.List;

@Data
@Builder
public class AsambleaConAsistenciaDTO {
    private Long id;
    private String titulo;
    private String descripcion;
    private Date fecha;
    private EstadoAsamblea estado;
    private String lugar;
    private LocalTime horaInicio;
    private List<Asistencia> propietarios;
}

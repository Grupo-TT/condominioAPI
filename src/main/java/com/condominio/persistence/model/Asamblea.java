package com.condominio.persistence.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalTime;
import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@ToString
public class Asamblea {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String titulo;
    private String descripcion;
    private Date fecha;
    @Enumerated(EnumType.STRING)
    private EstadoAsamblea estado;
    private String lugar;
    private LocalTime horaInicio;
}

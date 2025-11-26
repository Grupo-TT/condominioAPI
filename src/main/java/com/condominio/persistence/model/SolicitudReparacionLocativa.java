package com.condominio.persistence.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@Entity
public class SolicitudReparacionLocativa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private LocalDate fechaRealizacion;
    @Column
    private String motivo;
    @Column
    private String responsable;

    @Column
    private LocalDate inicioObra;

    @Column
    private LocalDate finObra;

    @Enumerated(EnumType.STRING)
    private EstadoSolicitud estadoSolicitud;

    @ManyToOne(targetEntity = Casa.class)
    @JoinColumn(nullable = false)
    private Casa casa;

    @Column
    private String comentarios;

    @Enumerated(EnumType.STRING)
    private TipoObra tipoObra;

    @Column
    private String tipoObraDetalle;
}

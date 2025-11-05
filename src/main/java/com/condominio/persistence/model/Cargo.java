package com.condominio.persistence.model;

import com.condominio.util.constants.AppConstants;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@NoArgsConstructor
@MappedSuperclass
@Getter
@Setter
public class Cargo {

    private double valorActual;
    private double nuevoValor;
    private String correoActualizador;
    private String nombreActualizador;
    private OffsetDateTime fechaAplicacion;

    public Cargo(double valorActual, double nuevoValor,
                 String correoActualizador,
                 String nombreActualizador) {
        this.valorActual = valorActual;
        this.nuevoValor = nuevoValor;
        this.correoActualizador = correoActualizador;
        this.nombreActualizador = nombreActualizador;
        this.fechaAplicacion = OffsetDateTime.now(AppConstants.ZONE);
    }
}

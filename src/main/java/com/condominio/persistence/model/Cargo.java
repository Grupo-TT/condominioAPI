package com.condominio.persistence.model;


import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@NoArgsConstructor
@MappedSuperclass
@Getter
@Setter
public class Cargo {

    private double valorActual;
    private double nuevoValor;
    private String correoActualizador;
    private String nombreActualizador;


    public Cargo(double valorActual, double nuevoValor,
                 String correoActualizador,
                 String nombreActualizador) {
        this.valorActual = valorActual;
        this.nuevoValor = nuevoValor;
        this.correoActualizador = correoActualizador;
        this.nombreActualizador = nombreActualizador;
    }
}

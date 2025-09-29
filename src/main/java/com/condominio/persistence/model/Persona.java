package com.condominio.persistence.model;

import jakarta.persistence.*;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public class Persona {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String primerNombre;
    private String segundoNombre;
    private String primerApellido;
    private String segundoApellido;

}

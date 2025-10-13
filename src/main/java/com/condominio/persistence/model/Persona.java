package com.condominio.persistence.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
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
    @Enumerated(EnumType.STRING)
    private TipoDocumento tipoDocumento;
    private Long numeroDocumento;
    private Long telefono;
    @OneToOne
    private UserEntity user;
    private Boolean estado;
    private Boolean junta;
    private Boolean comiteConvivencia;
    @ManyToOne
    private Casa casa;

    public String getNombreCompleto() {
        return Stream.of(primerNombre, segundoNombre, primerApellido, segundoApellido)
                .filter(Objects::nonNull)
                .filter(s -> !s.isBlank())
                .collect(Collectors.joining(" "));
    }
}

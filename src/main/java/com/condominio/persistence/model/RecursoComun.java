package com.condominio.persistence.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.stereotype.Service;

@Getter
@Setter
@Service
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class RecursoComun {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column
    private String nombre;
    @Column
    private String descripcion;
    @Enumerated(EnumType.STRING)
    private DisponibilidadRecurso disponibilidadRecurso;
    @Column(columnDefinition = "int default 0")
    private String capacidadMaximaUso;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_recurso_comun", nullable = false)
    private TipoRecursoComun tipoRecursoComun;
}

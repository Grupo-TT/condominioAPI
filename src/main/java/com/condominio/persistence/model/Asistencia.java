package com.condominio.persistence.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.stereotype.Service;

import java.util.Date;

@Getter
@Setter
@Service
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class Asistencia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Date fecha;
    @ManyToOne(targetEntity = Casa.class)
    private Casa casa;
    @ManyToOne(targetEntity = Asamblea.class)
    private Asamblea asamblea;
    private Boolean estado;
    private String nombreResponsable;

}

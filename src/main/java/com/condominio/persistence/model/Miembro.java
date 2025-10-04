package com.condominio.persistence.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;

@Getter
@Service
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class Miembro {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long  id;
    private Boolean estado;
    @ManyToOne(targetEntity = Casa.class)
    private Casa casa;

    private String nombre;
    private Long telefono;
    private String parentesco;

}

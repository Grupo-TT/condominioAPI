package com.condominio.persistence.model;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
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
public class Miembro extends Persona{
    private Boolean estado;
    @ManyToOne(targetEntity = Casa.class)
    private Casa casa;
}

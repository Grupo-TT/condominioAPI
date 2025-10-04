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
public class Casa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private int numeroCasa;

}

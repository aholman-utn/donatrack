package com.tp.donatrack.logistica.domain;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "camiones")
public class Camion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String patente;

    private String marca;
    private String modelo;

    private double volumen;
    private double altura;

    @Column(name = "capacidad_carga")
    private double capacidadCarga;
}

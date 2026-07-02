package com.tp.donatrack.logistica.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Camion {
    private Long id;
    private double volumen;
    private double altura;
    private String patente;
    private String marca;
    private String modelo;
    private double capacidadCarga;
}

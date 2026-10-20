package com.tp.donatrack.logistica.domain;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "camiones")
public class Camion {
    @Id
    private String id = null;

    private String patente;

    private String marca;
    private String modelo;

    private double volumen;
    private double altura;

    private double capacidadCarga;
}

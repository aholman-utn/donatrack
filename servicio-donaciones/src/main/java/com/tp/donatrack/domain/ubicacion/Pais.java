package com.tp.donatrack.domain.ubicacion;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "paises")
public class Pais {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pais")
    private Long id;

    @Column(name = "nombre")
    private String nombre;

    // Requerido por JPA.
    protected Pais() {
    }

    public Pais(String paisNombre) {
        this.nombre = paisNombre;
    }
}

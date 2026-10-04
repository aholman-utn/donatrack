package com.tp.donatrack.domain.ubicacion;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "direcciones")
public class Direccion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_direccion")
    private Long id;

    @Column(name = "calle1")
    private String calle1;

    @Column(name = "calle2")
    private String calle2;

    @Column(name = "altura")
    private int altura;

    @Column(name = "sin_altura")
    private boolean sinAltura;

    @Column(name = "piso")
    private int piso;

    @Column(name = "cuerpo")
    private int cuerpo;

    @Column(name = "departamento")
    private String departamento;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "id_ciudad")
    private Ciudad ciudad;

    public String getDireccion() {
        if (calle1 == null || calle1.trim().isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder(calle1.trim());

        if (!sinAltura && altura > 0) {
            sb.append(" ").append(altura);
        } else if (sinAltura) {
            sb.append(" S/N");
        }

        if (calle2 != null && !calle2.trim().isEmpty()) {
            sb.append(" (e/ ").append(calle2.trim()).append(")");
        }

        if (piso > 0) {
            sb.append(", Piso ").append(piso);
        }
        if (departamento != null && !departamento.trim().isEmpty()) {
            sb.append(" Depto ").append(departamento.trim());
        }

        if (ciudad != null) {
            sb.append(", ").append(ciudad.getNombre());
        }

        return sb.toString();
    }
}
package com.tp.donatrack.domain.ubicacion;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Direccion {
    private String calle1;
    private String calle2;
    private int altura;
    private boolean sinAltura;
    private int piso;
    private int cuerpo;
    private String departamento;
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
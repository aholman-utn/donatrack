package com.tp.donatrack.dtos;

import com.tp.donatrack.domain.ubicacion.Ciudad;
import com.tp.donatrack.domain.ubicacion.Direccion;
import com.tp.donatrack.domain.ubicacion.Pais;
import com.tp.donatrack.domain.ubicacion.Provincia;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DireccionRequest {
    private String calle1;
    private String calle2;
    private int altura;
    private boolean sinAltura;
    private int piso;
    private int cuerpo;
    private String departamento;
    private String ciudadNombre;
    private String provinciaNombre;
    private String paisNombre;

    public Direccion toDomain() {
        Direccion d = new Direccion();
        d.setCalle1(calle1);
        d.setCalle2(calle2);
        d.setAltura(altura);
        d.setSinAltura(sinAltura);
        d.setPiso(piso);
        d.setCuerpo(cuerpo);
        d.setDepartamento(departamento);

        if (paisNombre != null) {
            Pais pais = new Pais(paisNombre);
            Provincia provincia = new Provincia(provinciaNombre, pais);
            d.setCiudad(new Ciudad(ciudadNombre, provincia));
        }
        return d;
    }
}

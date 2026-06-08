package com.tp.donatrack.dtos.input.importacionCSV;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegistroDonanteDTO {

    private String tipoPersona;
    private String tipoDoc;
    private String documento;
    private String nombre;
    private String email;
    private String telefono;

    // Constructor completo para que el Lector pueda instanciarlo fácilmente línea por línea
    public RegistroDonanteDTO(String tipoPersona, String tipoDoc, String documento,
                              String nombre, String email, String telefono) {
        this.tipoPersona = tipoPersona;
        this.tipoDoc = tipoDoc;
        this.documento = documento;
        this.nombre = nombre;
        this.email = email;
        this.telefono = telefono;
    }
}

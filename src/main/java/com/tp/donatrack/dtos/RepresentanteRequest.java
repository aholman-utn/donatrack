package com.tp.donatrack.dtos;

import com.tp.donatrack.domain.persona.PersonaRepresentante;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RepresentanteRequest {
    private String nombre;
    private String apellido;
    private int nroDocumento;

    public PersonaRepresentante toDomain() {
        return new PersonaRepresentante(nombre, apellido, nroDocumento);
    }
}

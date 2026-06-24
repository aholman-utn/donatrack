package com.tp.donatrack.domain.persona;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class PersonaRepresentante {
    private String nombre;
    private String apellido;
    private int nroDocumento;
}

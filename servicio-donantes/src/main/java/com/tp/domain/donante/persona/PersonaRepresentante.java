package com.tp.domain.donante.persona;

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

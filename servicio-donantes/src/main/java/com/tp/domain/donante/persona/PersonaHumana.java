package com.tp.domain.donante.persona;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PersonaHumana extends Persona {
    private String nombre;
    private Genero genero;
    private String apellido;
    private Date fechaNacimiento;
    private Integer edad;
    private String nroDocumento;
}

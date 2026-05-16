package com.tp.donatrack.domain.persona;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PersonaHumana extends Persona {
    private String nombre;
    private String genero;
    private String apellido;
//    private Date fechaNacimiento;
    private Integer edad;
    private String nroDocumento;
}

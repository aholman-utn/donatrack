package com.tp.donatrack.domain.persona;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PersonaHumana extends Persona {
    private String nombre;
    private String genero;
    private String apellido;
    private Integer edad;
    private String nroDocumento;
}

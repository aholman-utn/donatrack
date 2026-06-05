package com.tp.donatrack.domain.ubicacion;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Ciudad {
    private String nombre;
    private Provincia provincia;


}

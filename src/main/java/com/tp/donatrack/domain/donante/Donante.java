package com.tp.donatrack.domain.donante;

import lombok.Getter;
import lombok.Setter;

import com.tp.donatrack.domain.persona.Persona;

@Getter
@Setter
public class Donante {
    private int id;
    private Persona persona;
}

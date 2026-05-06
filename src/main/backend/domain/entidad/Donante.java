package domain.entidad;

import lombok.Getter;
import lombok.Setter;

import domain.persona.Persona;

@Getter
@Setter
public class Donante {
    private int id;
    private Persona persona;
}

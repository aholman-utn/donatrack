package com.tp.donatrack.domain.donante;

import com.tp.donatrack.domain.persona.Persona;
import lombok.Getter;
import lombok.Setter;

import com.tp.donatrack.domain.persona.PersonaHumana;
import com.tp.donatrack.domain.persona.PersonaJuridica;
@Getter
@Setter
//Donante no deberia heredar Persona ? porque tiene un atributo que es persona ?
public class Donante {
    private Persona persona;
    private String password;
    public Donante(Persona persona) {
        this.persona = persona;
    }

    public Donante() {} //constructor sin atributos, en el futuro quizas deberia poner el ID

    public String getNombreCompleto() {
        if (this.persona instanceof PersonaHumana ph) {
            return ph.getNombre() + " " + ph.getApellido();
        } else if (this.persona instanceof PersonaJuridica pj) {
            return pj.getRazonSocial();
        }
        return "Donante Anónimo";
    }
}

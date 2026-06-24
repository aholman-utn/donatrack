package com.tp.domain.donante;

import com.tp.domain.donante.persona.Persona;
import com.tp.domain.donante.persona.PersonaHumana;
import com.tp.domain.donante.persona.PersonaJuridica;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
public class Donante {
    private Long id;
    private Persona persona;
    private String email;
    private String password;
    private Perfil perfil;

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

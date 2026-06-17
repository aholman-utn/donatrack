package com.tp.donatrack.domain.donante;

import com.tp.donatrack.domain.notificacion.Notificacion;
import com.tp.donatrack.domain.notificacion.TipoNotificacion;
import lombok.Getter;
import lombok.Setter;

import com.tp.donatrack.domain.persona.Persona;
import com.tp.donatrack.domain.persona.PersonaHumana;
import com.tp.donatrack.domain.persona.PersonaJuridica;

@Getter
@Setter
public class Donante {
    private Integer id;
    private Persona persona;
    private String password;
    public Donante(Persona persona) {
        this.persona = persona;
        this.persona.agregarNotificacion(
                new Notificacion(
                        "¡Bienvenido!",
                        "Gracias por registrarte como donante.",
                        "Bienvenida al sistema",
                        TipoNotificacion.BIENVENIDA
                )
        );
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

package com.tp.donatrack.domain.donante;

import com.tp.donatrack.domain.notificacion.Notificacion;
import com.tp.donatrack.domain.notificacion.TipoNotificacion;
import lombok.Getter;
import lombok.Setter;

import com.tp.donatrack.domain.persona.Persona;

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
}

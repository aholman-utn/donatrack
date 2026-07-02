package com.tp.donatrack.domain.donante;

import com.tp.donatrack.domain.persona.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class Donante {
    private Persona persona;
    private String password;

    @Builder.Default
    private PerfilDonante perfil = new PerfilDonante();

    public Donante(Persona persona) {
        this.persona = persona;
        this.perfil = new PerfilDonante();
    }

    public Donante() {
        this.perfil = new PerfilDonante();
    }

    public String getNombreCompleto() {
        if (this.persona instanceof PersonaHumana ph) {
            return ph.getNombre() + " " + ph.getApellido();
        } else if (this.persona instanceof PersonaJuridica pj) {
            return pj.getRazonSocial();
        }
        return "Donante Anónimo";
    }
}

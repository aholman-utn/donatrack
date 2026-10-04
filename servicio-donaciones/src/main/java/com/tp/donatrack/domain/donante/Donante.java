package com.tp.donatrack.domain.donante;

import com.tp.donatrack.domain.persona.*;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@Builder
@Entity
@Table(name = "donantes")
public class Donante {

    @Id
    @Column(name = "id_donante")
    private Long id;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "id_donante")
    @MapsId
    private Persona persona;

    @Column(name = "password_hash")
    private String password;

    // PerfilDonante pertenece al agregado de incentivos; se mapea en una fase
    // posterior. Por ahora se mantiene fuera de la persistencia JPA.
    @Transient
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

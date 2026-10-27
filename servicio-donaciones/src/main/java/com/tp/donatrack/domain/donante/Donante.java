package com.tp.donatrack.domain.donante;

import com.tp.donatrack.domain.persona.*;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
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

    /** Perfil de incentivos del donante con métricas e indicadores desnormalizados. */
    @Embedded
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

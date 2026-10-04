package com.tp.donatrack.domain.persona;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "personas_juridicas")
@PrimaryKeyJoinColumn(name = "id_persona")
public class PersonaJuridica extends Persona {

    @Column(name = "razon_social")
    private String razonSocial;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_organizacion")
    private TipoOrganizacion tipo;

    @Column(name = "cuit")
    private String cuit;

    @Column(name = "rubro")
    private String rubro;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = jakarta.persistence.FetchType.EAGER)
    @JoinColumn(name = "id_persona_juridica")
    private List<PersonaRepresentante> personasRepresentantes;

    public PersonaJuridica(){
        this.personasRepresentantes = new ArrayList<>();
    }

    public void agregarRepresentante(PersonaRepresentante representante){
        this.personasRepresentantes.add(representante);
    }

}

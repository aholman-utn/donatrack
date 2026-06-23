package com.tp.domain.donante.persona;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class PersonaJuridica extends Persona {
    private String razonSocial;
    private TipoOrganizacion tipo;
    private String cuit;
    private String rubro;
    private List<PersonaRepresentante> personasRepresentantes;

    public PersonaJuridica(){
        this.personasRepresentantes = new ArrayList<>();
    }

    public void agregarRepresentante(PersonaRepresentante representante){
        this.personasRepresentantes.add(representante);
    }

}

package com.tp.donatrack.domain.persona;

import com.tp.donatrack.domain.entidad.TipoOrganizacion;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class PersonaJuridica extends Persona {
    private String razonSocial;
    private TipoOrganizacion tipo;
    private String rubro;
    private List<PersonaRepresentante> personasRepresentantes;

    public PersonaJuridica(){
        this.personasRepresentantes = new ArrayList<>();
    }

    public void agregarRepresentante(PersonaRepresentante representante){
        this.personasRepresentantes.add(representante);
    }
}

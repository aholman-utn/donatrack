package com.tp.donatrack.domain.persona;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class PersonaJuridica extends Persona {
    private String razonSocial;
    private TipoOrganizacion tipo;
    private String rubro;
    private List<PersonaRepresentante> personasRepresentantes;
}

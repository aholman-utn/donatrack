package com.tp.donatrack.domain.persona;

import com.tp.donatrack.domain.entidad.TipoOrganizacion;
import java.util.List;

public class PersonaJuridica extends Persona {
    private String razonSocial;
    private TipoOrganizacion tipo;
    private String rubro;
    private List<PersonaRepresentante> personasRepresentantes;
}

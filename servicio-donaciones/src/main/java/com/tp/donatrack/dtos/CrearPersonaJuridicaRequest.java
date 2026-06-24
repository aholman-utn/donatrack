package com.tp.donatrack.dtos;

import com.tp.donatrack.domain.persona.PersonaJuridica;
import com.tp.donatrack.domain.persona.TipoOrganizacion;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class CrearPersonaJuridicaRequest {

    private Long id;

    @NotBlank(message = "La razón social es obligatoria")
    private String razonSocial;

    @NotNull(message = "El tipo de organización es obligatorio")
    private TipoOrganizacion tipo;

    private String rubro;
    private List<RepresentanteRequest> representantes;
    private DireccionRequest direccion;
    private List<ContactoValorRequest> mediosDeContacto;
    private Map<String, String> medioPredeterminado;

    public PersonaJuridica toDomain() {
        PersonaJuridica p = new PersonaJuridica();
        p.setId(id);
        p.setRazonSocial(razonSocial);
        p.setTipo(tipo);
        p.setRubro(rubro);
        p.setMedioPredeterminado(medioPredeterminado);

        if (direccion != null)
            p.setDireccion(direccion.toDomain());
        if (mediosDeContacto != null) {
            Map<String, List<String>> mediosMap = new java.util.HashMap<>();
            for (ContactoValorRequest cr : mediosDeContacto) {
                mediosMap.computeIfAbsent(cr.getMedio().name(), k -> new java.util.ArrayList<>()).add(cr.getValor());
            }
            p.setMedioDeContacto(mediosMap);
        }
        if (representantes != null)
            p.setPersonasRepresentantes(representantes.stream()
                    .map(RepresentanteRequest::toDomain).toList());
        return p;
    }
}

package com.tp.donatrack.dtos;

import com.tp.donatrack.domain.persona.PersonaHumana;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class CrearPersonaHumanaRequest {

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    private String apellido;

    private String genero;
    private Date fechaNacimiento;
    private int edad;
    private String nroDocumento;

    private DireccionRequest direccion;
    private List<ContactoValorRequest> mediosDeContacto;
    private Map<String, String> medioPredeterminado;

    public PersonaHumana toDomain() {
        PersonaHumana p = new PersonaHumana();
        p.setNombre(nombre);
        p.setApellido(apellido);
        p.setGenero(genero);
        p.setFechaNacimiento(fechaNacimiento);
        p.setEdad(edad);
        p.setNroDocumento(nroDocumento);
        p.setMedioPredeterminado(medioPredeterminado);

        if (direccion != null)        p.setDireccion(direccion.toDomain());
        if (mediosDeContacto != null) {
            Map<String, List<String>> mediosMap = new java.util.HashMap<>();
            for (ContactoValorRequest cr : mediosDeContacto) {
                mediosMap.computeIfAbsent(cr.getMedio().name(), k -> new java.util.ArrayList<>()).add(cr.getValor());
            }
            p.setMedioDeContacto(mediosMap);
        }
        return p;
    }
}

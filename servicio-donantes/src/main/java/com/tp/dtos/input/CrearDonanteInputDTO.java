package com.tp.dtos.input;

import com.tp.domain.donante.persona.Genero;
import com.tp.domain.donante.persona.TipoOrganizacion;
import com.tp.domain.donante.persona.TipoPersona;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CrearDonanteInputDTO {
    @NotBlank
    private String email;

    @NotBlank
    private String password;

    @NotBlank
    private TipoPersona tipoPersona;

    //Para crear persona humana
    private String nombre;
    private String apellido;
    private Date fechaNacimiento;
    private String nroDocumento;
    private Genero genero;

    //para crear persona juridica
    private String razonSocial;
    private TipoOrganizacion tipoOrganizacion;
    private String cuit;
    private String rubro;
}

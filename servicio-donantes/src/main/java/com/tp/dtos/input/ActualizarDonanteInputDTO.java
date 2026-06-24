package com.tp.dtos.input;

import com.tp.domain.donante.persona.Genero;
import com.tp.domain.donante.persona.TipoOrganizacion;
import com.tp.domain.donante.persona.ubicacion.Direccion;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class ActualizarDonanteInputDTO {
    //estos son los campos que el donante puede actualizar
    private String email;
    private String password;
    private String nuevo_password;
    private String nombre;
    private Genero genero;
    private String nroDocumento;
    private Map<String,String> medioPredeterminado;
    private Map<String,List<String>> mediosDeContacto;
    private Direccion direccion;
}

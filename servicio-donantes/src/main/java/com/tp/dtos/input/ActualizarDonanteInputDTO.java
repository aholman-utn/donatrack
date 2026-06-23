package com.tp.dtos.input;

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
    private Map<String,String> medioPredeterminado;
    private List<Map<String,String>> mediosDeContacto;
    private Direccion direccion;
}

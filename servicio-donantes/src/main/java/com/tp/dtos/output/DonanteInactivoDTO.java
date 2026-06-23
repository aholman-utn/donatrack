package com.tp.dtos.output;


import com.tp.domain.donante.persona.TipoNotificador;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DonanteInactivoDTO {
    private Long id;
    private String contacto;
    private TipoNotificador tipoNotificadorPreferido;
}
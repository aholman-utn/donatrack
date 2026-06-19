package com.tp.donatrack.dtos;

import com.tp.donatrack.domain.notificador.TipoNotificador;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DonanteInactivoDTO {
    private Long id;
    private String contacto; 
    private TipoNotificador tipoNotificadorPreferido;
}
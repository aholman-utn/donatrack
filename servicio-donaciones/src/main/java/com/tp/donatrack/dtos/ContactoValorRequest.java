package com.tp.donatrack.dtos;

import com.tp.commons.domain.notificador.TipoNotificador;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ContactoValorRequest {
    private TipoNotificador medio;
    private String valor;
}

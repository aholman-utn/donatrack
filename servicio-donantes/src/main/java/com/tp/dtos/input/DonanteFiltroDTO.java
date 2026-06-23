package com.tp.dtos.input;

import com.tp.domain.donante.persona.TipoPersona;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DonanteFiltroDTO {
    //todos estos atributos son las query params
    //ej: GET /donantes?tipoPersona=JURIDICA&fechaUltimaInteraccion=2025-01-01
    private String email;
    private TipoPersona tipoPersona;
    private LocalDateTime fechaUltimaInteraccion;
}

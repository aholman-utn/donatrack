package com.tp.dtos.output;

import com.tp.domain.donante.Perfil;
import com.tp.domain.donante.persona.Persona;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
public class DonanteOutputDTO {
    private Long id;
    private Persona persona;
    private Perfil perfil;
    private LocalDateTime fechaUltimaInteraccion;
}

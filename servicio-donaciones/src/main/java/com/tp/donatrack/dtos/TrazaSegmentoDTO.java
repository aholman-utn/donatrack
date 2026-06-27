package com.tp.donatrack.dtos;

import com.tp.donatrack.domain.trazabilidad.EventoTrazabilidad;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrazaSegmentoDTO {
    private Integer id;
    private List<EventoTrazabilidad> eventos;
}

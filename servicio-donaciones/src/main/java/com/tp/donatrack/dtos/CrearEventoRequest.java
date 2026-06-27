package com.tp.donatrack.dtos;

import com.tp.donatrack.domain.donacion.EstadoDonacionSegmentada;
import lombok.Getter;

@Getter
public class CrearEventoRequest {
    private EstadoDonacionSegmentada nuevoEstado;
    private String actor;
    private String descripcion;
}

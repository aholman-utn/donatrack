package com.tp.donatrack.dtos;

import com.tp.donatrack.domain.donacion.EstadoDonacion;
import com.tp.donatrack.domain.trazabilidad.EventoTrazabilidad;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrazaDonacionDTO {
    private Integer id;
    private EstadoDonacion estado;
    private List<TrazaSegmentoDTO> segmentos;
}

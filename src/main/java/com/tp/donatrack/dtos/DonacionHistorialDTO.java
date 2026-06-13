package com.tp.donatrack.dtos;

import com.tp.donatrack.domain.donacion.EstadoDonacion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DonacionHistorialDTO {
    private String descripcion;
    private Date fechaIngreso;
    private EstadoDonacion estado;
    private List<DonacionSegmentadaHistorialDTO> donacionesSegmentadas;
}

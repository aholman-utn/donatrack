package com.tp.donatrack.dtos;

import com.tp.donatrack.domain.donacion.EstadoDonacionSegmentada;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DonacionSegmentadaHistorialDTO {
    private Integer id;
    private String subCategoria;
    private int cantidad;
    private EstadoDonacionSegmentada estado;
    private Integer entidadBeneficiariaAsignadaId;
}

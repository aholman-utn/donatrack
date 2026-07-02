package com.tp.commons.dtos.incentivos;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class IndicadoresDonanteDTO {
    private Integer cantidadBienesTotal;
    private Integer cantidadCategoriasUnicas;
    private Integer mesesConsecutivosRacha;
    private Integer cantidadDonacionesEntregadas;
}
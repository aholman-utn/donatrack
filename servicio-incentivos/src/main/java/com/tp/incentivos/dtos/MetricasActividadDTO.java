package com.tp.incentivos.dtos;

import java.util.List;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MetricasActividadDTO {
    private Integer donanteId;
    private String categoriaDonante;

    // Métricas acumuladas
    private int totalDonacionesExitosas;
    private int entidadesAyudadasCount;
    private List<Integer> entidadesAyudadasIds;
    private int posicionRanking;

    // Métricas del período actual (mes en curso)
    private int donacionesMesActual;
    private int mesPeriodoActual;
    private int anioPeriodoActual;

    // Historial mensual
    private List<RegistroDonacionMensualDTO> comparacionesMensuales;
}

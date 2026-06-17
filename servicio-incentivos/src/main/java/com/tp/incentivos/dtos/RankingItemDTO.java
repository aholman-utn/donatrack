package com.tp.incentivos.dtos;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RankingItemDTO {
    private int posicion;
    private Integer donanteId;
    private int totalDonacionesExitosas;
    private String categoriaDonante;
    private int totalMisionesCompletadas;
}

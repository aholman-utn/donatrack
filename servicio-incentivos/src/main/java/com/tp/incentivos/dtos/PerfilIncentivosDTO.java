package com.tp.incentivos.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PerfilIncentivosDTO {
    private Long donanteId;
    private int totalDonacionesExitosas;
    private int entidadesAyudadasCount;
    private Set<Long> entidadesAyudadasIds;
}

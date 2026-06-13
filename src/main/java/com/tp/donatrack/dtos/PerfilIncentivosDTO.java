package com.tp.donatrack.dtos;

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
public class PerfilIncentivosDTO {
    private Integer donanteId;
    private int totalesHistoricosDonaciones;
    private int organizacionesAyudadasCount;
    private int posicionRanking;
}

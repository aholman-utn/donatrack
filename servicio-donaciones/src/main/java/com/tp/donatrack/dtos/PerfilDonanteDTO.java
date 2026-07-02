package com.tp.donatrack.dtos;

import java.util.List;

import com.tp.commons.domain.donantes.Nivel;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PerfilDonanteDTO {
    private boolean visibilidadInsignia;
    private Nivel categoriaDonante;
    private Long misionActualId;
    private Double progreso;
    private List<String> insigniasGanadas;
    private List<Long> misionesCompletadasIds;
}

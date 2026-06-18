package com.tp.donatrack.dtos;

import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResultadoMatchmakingDTO {
    private Integer donacionSegmentadaId;
    private String subCategoria;
    private int cantidadBienes;

    private List<EntidadRankingDTO> coincidencias;
    private List<EntidadRankingDTO> rankingCompatibilidad;
    private List<EntidadRankingDTO> rankingSubAtendidos;
    private List<EntidadRankingDTO> entidadesPropuestas;

    private boolean huboCoincidencias;
}

package com.tp.donatrack.dtos;

import lombok.*;
import java.util.List;

/**
 * DTO que encapsula el resultado del proceso de matchmaking para una donación segmentada.
 * Contiene las entidades propuestas por cada algoritmo, las coincidencias entre ambos,
 * y la lista final de entidades recomendadas.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResultadoMatchmakingDTO {
    private Integer donacionSegmentadaId;
    private String subCategoria;
    private int cantidadBienes;

    /** Entidades que ambos algoritmos propusieron (intersección) */
    private List<EntidadRankingDTO> coincidencias;

    /** Ranking según compatibilidad semántica (subcategoría vs necesidad) */
    private List<EntidadRankingDTO> rankingCompatibilidad;

    /** Ranking según prioridad a sub-atendidos (menos donaciones recibidas) */
    private List<EntidadRankingDTO> rankingSubAtendidos;

    /** Lista final recomendada: coincidencias si hay, sino compatibilidad */
    private List<EntidadRankingDTO> entidadesPropuestas;

    private boolean huboCoincidencias;
}

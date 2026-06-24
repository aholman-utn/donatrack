package com.tp.donatrack.dtos;

import lombok.*;

/**
 * Representa una entidad beneficiaria dentro del ranking de matchmaking.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EntidadRankingDTO {
    private Integer id;
    private String razonSocial;
    private int cantNecesidadesActivas;
}

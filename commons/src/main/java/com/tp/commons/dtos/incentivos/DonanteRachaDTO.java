package com.tp.commons.dtos.incentivos;

import com.tp.commons.domain.donantes.Nivel;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DonanteRachaDTO {
    private Long donanteId;
    private Long misionActualId;
    private Nivel nivel;
}

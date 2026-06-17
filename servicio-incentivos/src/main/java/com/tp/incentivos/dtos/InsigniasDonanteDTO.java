package com.tp.incentivos.dtos;

import java.util.List;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InsigniasDonanteDTO {
    private Integer donanteId;
    private Boolean visibilidadInsignia;
    private List<InsigniaDTO> insignias;
    private int totalInsignias;
}

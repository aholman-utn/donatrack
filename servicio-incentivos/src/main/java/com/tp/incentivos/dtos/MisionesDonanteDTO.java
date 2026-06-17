package com.tp.incentivos.dtos;

import java.util.List;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MisionesDonanteDTO {
    private MisionDTO misionActual;
    private List<MisionDTO> proximasMisiones;
    private List<MisionDTO> misionesCompletadas;
}

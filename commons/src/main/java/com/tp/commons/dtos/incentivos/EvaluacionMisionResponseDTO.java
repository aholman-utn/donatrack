package com.tp.commons.dtos.incentivos;

import com.tp.commons.domain.donantes.Nivel;
import com.tp.commons.domain.incentivos.Insignia;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class EvaluacionMisionResponseDTO {
    private boolean misionCumplida;
    private Double nuevoProgreso;
    private Long siguienteMisionId;
    private Insignia insigniaGanada;
    private boolean subioDeCategoria;
    private Nivel nuevoNivel;
}
package com.tp.donatrack.domain.donante;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EntidadAyudada {
    private Long entidadBeneficiariaId;
    private Long donacionSegmentadaId;
}
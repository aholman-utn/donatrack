package com.tp.incentivos.domain;

import java.time.LocalDate;
import java.util.List;
import lombok.*;

// encapsula todos los datos de una entreg para ser evaluados por el sistema de misiones.
@Getter
@Setter
@Builder
@AllArgsConstructor
public class InfoDonacion {

    private final Integer donanteId;
    private final Integer entidadBeneficiariaId;
    private final int cantidadBienes;
    private final LocalDate fechaDonacion;
    private final CategoriaBien categoriaDonacion;
    private final List<CategoriaBien> categoriasAcumuladas;

}

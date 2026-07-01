package com.tp.donatrack.domain.donante;

import com.tp.donatrack.domain.bien.CategoriaBien;
import java.time.LocalDate;
import com.tp.donatrack.domain.donacion.EstadoDonacionSegmentada;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemHistoralDonaciones {
    private Long id;
    private Long entidadBeneficiariaId;
    private LocalDate fecha;
    private CategoriaBien categoria;
    private EstadoDonacionSegmentada estado;
}

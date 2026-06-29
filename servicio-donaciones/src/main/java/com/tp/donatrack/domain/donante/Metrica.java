package com.tp.donatrack.domain.donante;

import com.tp.donatrack.domain.bien.CategoriaBien;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Metrica {
    private Long totalDonacionesExitosas;
    private List<CategoriaBien> categoriasAyudadas;
    private List<EntidadAyudada> entidadesAyudadas;
    private List<Long> misionesCompletadas;
}

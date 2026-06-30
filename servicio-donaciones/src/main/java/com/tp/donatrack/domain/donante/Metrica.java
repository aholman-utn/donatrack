package com.tp.donatrack.domain.donante;

import com.tp.donatrack.domain.bien.CategoriaBien;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Metrica {
    private Integer totalDonacionesExitosas;
    private List<CategoriaBien> categoriasAyudadas;
    private List<EntidadAyudada> entidadesAyudadas;
    private List<Long> misionesCompletadas;

    public Metrica() {
        this.totalDonacionesExitosas = 0;
        this.categoriasAyudadas = new ArrayList<>();
        this.entidadesAyudadas = new ArrayList<>();
        this.misionesCompletadas = new ArrayList<>();
    }
}

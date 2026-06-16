package com.tp.donatrack.domain.incentivos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PerfilIncentivosDonante {
    private Integer donanteId;
    private int totalesHistoricosDonaciones;
    private int organizacionesAyudadasCount;
    private int posicionRanking;

    public void incrementarDonaciones() {
        this.totalesHistoricosDonaciones++;
    }

    public void incrementarOrganizaciones() {
        this.organizacionesAyudadasCount++;
    }

    public void actualizarRanking(int nuevaPosicion) {
        this.posicionRanking = nuevaPosicion;
    }
}

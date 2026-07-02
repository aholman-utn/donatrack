package com.tp.donatrack.logistica.domain.planificacion;

import com.tp.commons.dtos.logistica.DonacionSegmentadaListaParaEntregarALogisticaDTO;
import com.tp.donatrack.logistica.domain.Camion;
import com.tp.donatrack.logistica.domain.Chofer;
import com.tp.donatrack.logistica.domain.Ruta;

import java.util.List;

public interface Planificacion{
    List<Ruta> planificar(
            List<DonacionSegmentadaListaParaEntregarALogisticaDTO> donaciones,
            List<Camion> camionesDisponibles,
            List<Chofer> choferesDisponibles
    );
}
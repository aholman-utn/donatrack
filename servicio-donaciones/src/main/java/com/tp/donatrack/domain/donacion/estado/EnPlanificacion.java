package com.tp.donatrack.domain.donacion.estado;

import com.tp.donatrack.domain.donacion.DonacionSegmentada;
import com.tp.donatrack.domain.donacion.EstadoDonacionSegmentada;

public class EnPlanificacion extends EstadoDonacionSegmentada {

    @Override
    public String getNombre() {
        return "EN_PLANIFICACION";
    }

    @Override
    public boolean isAsignada() {
        return true;
    }

    @Override
    public boolean puedeTransicionarA(EstadoDonacionSegmentada nuevo) {
        return nuevo != null && "EN_TRASLADO".equals(nuevo.getNombre());
    }

    @Override
    public void iniciarTraslado(DonacionSegmentada donacion, String actor) {
        donacion.transicionar(
                new EnTraslado(),
                actor,
                "Camión inició el recorrido de entrega"
        );
    }
}
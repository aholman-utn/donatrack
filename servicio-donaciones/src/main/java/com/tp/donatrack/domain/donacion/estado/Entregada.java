package com.tp.donatrack.domain.donacion.estado;

import com.tp.donatrack.domain.donacion.EstadoDonacionSegmentada;

public class Entregada extends EstadoDonacionSegmentada {

    @Override
    public String getNombre() {
        return "ENTREGADA";
    }

    @Override
    public boolean isAsignada() {
        return true;
    }

    @Override
    public boolean isFinalizada() {
        return true;
    }
}

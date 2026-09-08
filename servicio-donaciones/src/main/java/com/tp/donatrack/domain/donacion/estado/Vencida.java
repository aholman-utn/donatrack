package com.tp.donatrack.domain.donacion.estado;

import com.tp.donatrack.domain.donacion.EstadoDonacionSegmentada;

public class Vencida extends EstadoDonacionSegmentada {

    @Override
    public String getNombre() {
        return "VENCIDA";
    }

    @Override
    public boolean isFinalizada() {
        return true;
    }
}

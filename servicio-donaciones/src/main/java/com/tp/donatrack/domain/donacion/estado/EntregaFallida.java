package com.tp.donatrack.domain.donacion.estado;

import com.tp.donatrack.domain.donacion.DonacionSegmentada;
import com.tp.donatrack.domain.donacion.EstadoDonacionSegmentada;

public class EntregaFallida extends EstadoDonacionSegmentada {

    @Override
    public String getNombre() {
        return "ENTREGA_FALLIDA";
    }

    @Override
    public boolean puedeTransicionarA(EstadoDonacionSegmentada nuevo) {
        return nuevo != null && "EN_DEPOSITO".equals(nuevo.getNombre());
    }

    public void reingresarADeposito(DonacionSegmentada donacion, String actor) {
        donacion.transicionar(new EnDeposito(), actor, "Donación devuelta al depósito tras entrega fallida");
    }
}

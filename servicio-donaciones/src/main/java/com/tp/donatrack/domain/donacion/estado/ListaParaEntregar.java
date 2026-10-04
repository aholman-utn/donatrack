package com.tp.donatrack.domain.donacion.estado;

import com.tp.donatrack.domain.donacion.DonacionSegmentada;
import com.tp.donatrack.domain.donacion.EstadoDonacionSegmentada;

public class ListaParaEntregar extends EstadoDonacionSegmentada {

    @Override
    public String getNombre() {
        return "LISTA_PARA_ENTREGAR";
    }

    @Override
    public boolean isAsignada() {
        return true;
    }

    @Override
    public boolean isListaParaEntregar() {
        return true;
    }

    @Override
    public void solicitarPlanificacion(DonacionSegmentada donacion, String actor) {
        donacion.transicionar(new EnPlanificacion(),actor,"Lote enviado a logística para planificación");
    }

    @Override
    public boolean puedeTransicionarA(EstadoDonacionSegmentada nuevo) {
        return nuevo != null && "EN_PLANIFICACION".equals(nuevo.getNombre());
    }
}

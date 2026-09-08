package com.tp.donatrack.domain.donacion.estado;

import com.tp.donatrack.domain.donacion.DonacionSegmentada;
import com.tp.donatrack.domain.donacion.EstadoDonacionSegmentada;

public class AsignacionRealizada extends EstadoDonacionSegmentada {

    @Override
    public String getNombre() {
        return "ASIGNACION_REALIZADA";
    }

    @Override
    public boolean isAsignada() {
        return true;
    }

    @Override
    public boolean puedeTransicionarA(EstadoDonacionSegmentada nuevo) {
        return nuevo != null && "LISTA_PARA_ENTREGAR".equals(nuevo.getNombre());
    }

    @Override
    public void listarParaEntrega(DonacionSegmentada donacion, String actor) {
        donacion.transicionar(new ListaParaEntregar(), actor, "Ruta de entrega planificada");
    }
}

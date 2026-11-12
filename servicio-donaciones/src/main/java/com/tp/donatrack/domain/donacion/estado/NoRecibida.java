package com.tp.donatrack.domain.donacion.estado;

import com.tp.donatrack.domain.donacion.EstadoDonacionSegmentada;

/**
 * La entidad beneficiaria informó que no recibió una donación que estaba en
 * traslado. Queda pendiente de revisión por administración, que decide si se
 * vuelve a planificar (EN_DEPOSITO) o si finalmente se dio por entregada.
 */
public class NoRecibida extends EstadoDonacionSegmentada {

    @Override
    public String getNombre() {
        return "NO_RECIBIDA";
    }

    @Override
    public boolean isAsignada() {
        return true;
    }

    @Override
    public boolean puedeTransicionarA(EstadoDonacionSegmentada nuevo) {
        if (nuevo == null) return false;
        String n = nuevo.getNombre();
        return "EN_DEPOSITO".equals(n) || "ENTREGADA".equals(n);
    }
}

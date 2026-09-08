package com.tp.donatrack.domain.donacion.estado;

import com.tp.donatrack.domain.donacion.DonacionSegmentada;
import com.tp.donatrack.domain.donacion.EstadoDonacionSegmentada;
import com.tp.donatrack.domain.entidad.EntidadBeneficiaria;

public class EnDeposito extends EstadoDonacionSegmentada {

    @Override
    public String getNombre() {
        return "EN_DEPOSITO";
    }

    @Override
    public boolean isEnDeposito() {
        return true;
    }

    @Override
    public boolean puedeTransicionarA(EstadoDonacionSegmentada nuevo) {
        if (nuevo == null) return false;
        String n = nuevo.getNombre();
        return "ASIGNACION_REALIZADA".equals(n) || "VENCIDA".equals(n);
    }

    @Override
    public void asignar(DonacionSegmentada donacion, EntidadBeneficiaria entidad, String actor) {
        entidad.implementarDonacion(donacion);
        if (entidad.getDatosDeEntidad() != null) {
            donacion.setEntidadBeneficiariaAsignadaId(entidad.getDatosDeEntidad().getId());
        }
        donacion.transicionar(new AsignacionRealizada(), actor, "Donación asignada a entidad beneficiaria");
    }

    @Override
    public void marcarVencida(DonacionSegmentada donacion, String actor) {
        donacion.transicionar(new Vencida(), actor, "Donación marcada como vencida por administrador");
    }
}

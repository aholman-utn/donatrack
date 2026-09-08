package com.tp.donatrack.domain.donacion.estado;

import com.tp.donatrack.domain.donacion.DonacionSegmentada;
import com.tp.donatrack.domain.donacion.EstadoDonacionSegmentada;

public class EnTraslado extends EstadoDonacionSegmentada {

    @Override
    public String getNombre() {
        return "EN_TRASLADO";
    }

    @Override
    public boolean isAsignada() {
        return true;
    }

    @Override
    public boolean isEnTraslado() {
        return true;
    }

    @Override
    public boolean puedeTransicionarA(EstadoDonacionSegmentada nuevo) {
        if (nuevo == null) return false;
        String n = nuevo.getNombre();
        return "ENTREGADA".equals(n) || "ENTREGA_FALLIDA".equals(n);
    }

    @Override
    public void confirmarEntrega(DonacionSegmentada donacion, Long entidadBeneficiariaId) {
        donacion.transicionar(
                new Entregada(),
                String.valueOf(entidadBeneficiariaId),
                "Entidad beneficiaria confirmó la recepción"
        );
    }

    @Override
    public void registrarEntregaFallida(DonacionSegmentada donacion, String actor, String justificacion) {
        donacion.transicionar(new EntregaFallida(), actor, justificacion);
        // Regla de negocio: devuelta a depósito tras entrega fallida
        donacion.transicionar(new EnDeposito(), "Sistema", "Donación devuelta al depósito tras entrega fallida");
    }

    @Override
    public void registrarLlegadaADestino(DonacionSegmentada donacion, String actor) {
        donacion.registrarEventoTrazabilidad(
                this,
                this,
                actor,
                "El vehículo de logística reportó la llegada. Esperando confirmación de la entidad."
        );
    }
}

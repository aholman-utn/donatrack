package com.tp.donatrack.domain.donacion;

import com.tp.donatrack.domain.bien.SubCategoria;
import com.tp.donatrack.domain.entidad.EntidadBeneficiaria;
import com.tp.donatrack.domain.bien.Bien;
import com.tp.donatrack.domain.trazabilidad.EventoTrazabilidad;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
@Setter
public class DonacionSegmentada {
    private int cantidad;
    private SubCategoria subCategoria;
    private List<Bien> bienes;
    private EstadoDonacionSegmentada estado;
    private final List<EventoTrazabilidad> historial = new ArrayList<>();

    public DonacionSegmentada(
        int cantidad,
        SubCategoria subCategoria,
        List<Bien> bienes
    ) {
        this.cantidad = cantidad;
        this.subCategoria = subCategoria;
        this.bienes = bienes;
        this.estado = EstadoDonacionSegmentada.PENDIENTE;
        registrarEvento(null, EstadoDonacionSegmentada.PENDIENTE, "Sistema", "Donación registrada en el sistema");
    }

    public void transicionar(EstadoDonacionSegmentada nuevoEstado, String actor, String descripcion) {
        EstadoDonacionSegmentada anterior = this.estado;
        this.estado = nuevoEstado;
        registrarEvento(anterior, nuevoEstado, actor, descripcion);
    }

    public void donar(EntidadBeneficiaria entidad) {
        try {
            entidad.implementarDonacion(this);
            transicionar(EstadoDonacionSegmentada.ADJUDICADA, "Administrador", "Donación asignada a entidad beneficiaria");
        } catch (RuntimeException e) {
            System.err.println("Error al procesar la donación: " + e.getMessage());
        }
    }

    public void marcarEnTransito(String actor) {
        transicionar(EstadoDonacionSegmentada.EN_TRANSITO, actor, "Donación despachada en camión");
    }

    public void confirmarEntrega(String actor) {
        transicionar(EstadoDonacionSegmentada.ENTREGADA, actor, "Entidad beneficiaria confirmó la recepción");
    }

    public void marcarVencida() {
        transicionar(EstadoDonacionSegmentada.VENCIDA, "Sistema", "Bien perecedero vencido en depósito");
    }

    public List<EventoTrazabilidad> getHistorial() {
        return Collections.unmodifiableList(historial);
    }

    public EventoTrazabilidad getUltimoEvento() {
        if (historial.isEmpty()) return null;
        return historial.get(historial.size() - 1);
    }

    private void registrarEvento(EstadoDonacionSegmentada anterior, EstadoDonacionSegmentada nuevo, String actor, String descripcion) {
        historial.add(new EventoTrazabilidad(anterior, nuevo, actor, descripcion));
    }
}

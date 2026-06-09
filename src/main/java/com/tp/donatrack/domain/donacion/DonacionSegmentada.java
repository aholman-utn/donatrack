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
        this.estado = EstadoDonacionSegmentada.EN_DEPOSITO;
        registrarEvento(null, EstadoDonacionSegmentada.EN_DEPOSITO, "Administrador", "Donación registrada e ingresada al depósito");
    }

    public void transicionar(EstadoDonacionSegmentada nuevoEstado, String actor, String descripcion) {
        EstadoDonacionSegmentada anterior = this.estado;
        this.estado = nuevoEstado;
        registrarEvento(anterior, nuevoEstado, actor, descripcion);
    }

    public void asignar(EntidadBeneficiaria entidad, String actor) {
        entidad.implementarDonacion(this);
        transicionar(EstadoDonacionSegmentada.ASIGNACION_REALIZADA, actor, "Donación asignada a entidad beneficiaria");
    }

    /** @deprecated Usar asignar(entidad, actor) para trazabilidad completa */
    public void donar(EntidadBeneficiaria entidad) {
        asignar(entidad, "Administrador");
    }

    public void listarParaEntrega(String actor) {
        transicionar(EstadoDonacionSegmentada.LISTA_PARA_ENTREGAR, actor, "Ruta de entrega planificada");
    }

    public void iniciarTraslado(String actor) {
        transicionar(EstadoDonacionSegmentada.EN_TRASLADO, actor, "Camión inició el recorrido de entrega");
    }

    public void confirmarEntrega(String actor) {
        transicionar(EstadoDonacionSegmentada.ENTREGADA, actor, "Entidad beneficiaria confirmó la recepción");
    }

    public void registrarEntregaFallida(String actor, String justificacion) {
        transicionar(EstadoDonacionSegmentada.ENTREGA_FALLIDA, actor, justificacion);
        // Vuelve al depósito
        transicionar(EstadoDonacionSegmentada.EN_DEPOSITO, "Sistema", "Donación devuelta al depósito tras entrega fallida");
    }

    public void marcarVencida(String actor) {
        transicionar(EstadoDonacionSegmentada.VENCIDA, actor, "Donación marcada como vencida por administrador");
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

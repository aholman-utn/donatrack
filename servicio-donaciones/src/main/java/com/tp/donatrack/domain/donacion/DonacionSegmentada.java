package com.tp.donatrack.domain.donacion;

import com.tp.donatrack.domain.bien.Bien;
import com.tp.donatrack.domain.bien.SubCategoria;
import com.tp.donatrack.domain.donacion.estado.EnDeposito;
import com.tp.donatrack.domain.entidad.EntidadBeneficiaria;
import com.tp.donatrack.domain.trazabilidad.EventoTrazabilidad;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
@Setter
public class DonacionSegmentada {
    private Long id;
    private int cantidad; //300 kg de fideos, 200lt de leche...etc
    private SubCategoria subCategoria;
    private List<Bien> bienes;
    private EstadoDonacionSegmentada estado;
    private final List<EventoTrazabilidad> historial = new ArrayList<>();
    private Long donanteId;
    private Long entidadBeneficiariaAsignadaId;
    private ComprobanteRecepcionDonacion comprobanteRecepcionDonacion;

    public DonacionSegmentada(
            int cantidad,
            SubCategoria subCategoria,
            List<Bien> bienes) {
        this(cantidad, subCategoria, bienes, null);
    }

    public DonacionSegmentada(
            int cantidad,
            SubCategoria subCategoria,
            List<Bien> bienes,
            Long donanteId) {
        this.cantidad = cantidad;
        this.subCategoria = subCategoria;
        this.bienes = bienes;
        this.donanteId = donanteId;
        this.estado = new EnDeposito();
        registrarEvento(null, this.estado, "Administrador",
                "Donación registrada e ingresada al depósito");
    }

    public void transicionar(EstadoDonacionSegmentada nuevoEstado, String actor, String descripcion) {
        EstadoDonacionSegmentada anterior = this.estado;
        this.estado = nuevoEstado;
        registrarEvento(anterior, nuevoEstado, actor, descripcion);
    }

    public void asignar(EntidadBeneficiaria entidad, String actor) {
        this.estado.asignar(this, entidad, actor);
    }

    /** @deprecated Usar asignar(entidad, actor) para trazabilidad completa */
    @Deprecated
    public void donar(EntidadBeneficiaria entidad) {
        asignar(entidad, "Administrador");
    }

    public void listarParaEntrega(String actor) {
        this.estado.listarParaEntrega(this, actor);
    }

    public void iniciarTraslado(String actor) {
        this.estado.iniciarTraslado(this, actor);
    }

    public void confirmarEntrega(Long entidadBeneficiariaId) {
        this.estado.confirmarEntrega(this, entidadBeneficiariaId);
    }

    public void registrarEntregaFallida(String actor, String justificacion) {
        this.estado.registrarEntregaFallida(this, actor, justificacion);
    }

    public void marcarVencida(String actor) {
        this.estado.marcarVencida(this, actor);
    }

    public void registrarLlegadaADestino(String actor) {
        this.estado.registrarLlegadaADestino(this, actor);
    }

    public List<EventoTrazabilidad> getHistorial() {
        return Collections.unmodifiableList(historial);
    }

    public EventoTrazabilidad getUltimoEvento() {
        if (historial.isEmpty())
            return null;
        return historial.get(historial.size() - 1);
    }

    public boolean transicionPosible(EstadoDonacionSegmentada anterior, EstadoDonacionSegmentada nuevo) {
        return anterior != null && anterior.puedeTransicionarA(nuevo);
    }

    public void registrarEventoTrazabilidad(EstadoDonacionSegmentada anterior, EstadoDonacionSegmentada nuevo, String actor, String descripcion) {
        registrarEvento(anterior, nuevo, actor, descripcion);
    }

    private void registrarEvento(EstadoDonacionSegmentada anterior, EstadoDonacionSegmentada nuevo, String actor,
            String descripcion) {
        historial.add(new EventoTrazabilidad(anterior, nuevo, actor, descripcion));
    }
}

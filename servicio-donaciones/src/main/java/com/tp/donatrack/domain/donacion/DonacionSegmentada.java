package com.tp.donatrack.domain.donacion;

import com.tp.donatrack.domain.bien.SubCategoria;
import com.tp.donatrack.domain.entidad.EntidadBeneficiaria;
import com.tp.donatrack.domain.bien.Bien;
import com.tp.donatrack.domain.trazabilidad.EventoTrazabilidad;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
@Setter
public class DonacionSegmentada {
    private Integer id;
    private int cantidad;
    private SubCategoria subCategoria;
    private List<Bien> bienes;
    private EstadoDonacionSegmentada estado;
    private final List<EventoTrazabilidad> historial = new ArrayList<>();
    private Long donanteId;
    private Long entidadBeneficiariaAsignadaId;

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
        this.estado = EstadoDonacionSegmentada.EN_DEPOSITO;
        registrarEvento(null, EstadoDonacionSegmentada.EN_DEPOSITO, "Administrador",
                "Donación registrada e ingresada al depósito");
    }

    public void transicionar(EstadoDonacionSegmentada nuevoEstado, String actor, String descripcion) {
        EstadoDonacionSegmentada anterior = this.estado;
        this.estado = nuevoEstado;
        registrarEvento(anterior, nuevoEstado, actor, descripcion);
    }

    public void asignar(EntidadBeneficiaria entidad, String actor) {
        entidad.implementarDonacion(this);
        this.entidadBeneficiariaAsignadaId = entidad.getDatosDeEntidad().getId();
        transicionar(EstadoDonacionSegmentada.ASIGNACION_REALIZADA, actor, "Donación asignada a entidad beneficiaria");
    }

    /** @deprecated Usar asignar(entidad, actor) para trazabilidad completa */
    @Deprecated
    public void donar(EntidadBeneficiaria entidad) {
        asignar(entidad, "Administrador");
    }

    public void listarParaEntrega(String actor) {
        transicionar(EstadoDonacionSegmentada.LISTA_PARA_ENTREGAR, actor, "Ruta de entrega planificada");
    }

    public void iniciarTraslado(String actor) {
        transicionar(EstadoDonacionSegmentada.EN_TRASLADO, actor, "Camión inició el recorrido de entrega");
    }

    public void confirmarEntrega(Long entidadBeneficiariaId) {
        confirmarEntrega(entidadBeneficiariaId, null);
    }

    public void confirmarEntrega(Long entidadBeneficiariaId, DonacionEventPublisher eventPublisher) {
        transicionar(EstadoDonacionSegmentada.ENTREGADA, String.valueOf(entidadBeneficiariaId),
                "Entidad beneficiaria confirmó la recepción");
        if (eventPublisher != null && this.donanteId != null) {
            eventPublisher.publicar(new DonacionEntregadaEvent(this.donanteId, entidadBeneficiariaId,
                    this.subCategoria.getCategoria(), LocalDate.now()));
        }
    }

    public void registrarEntregaFallida(String actor, String justificacion) {
        transicionar(EstadoDonacionSegmentada.ENTREGA_FALLIDA, actor, justificacion);
        // Vuelve al depósito
        transicionar(EstadoDonacionSegmentada.EN_DEPOSITO, "Sistema",
                "Donación devuelta al depósito tras entrega fallida");
    }

    public void marcarVencida(String actor) {
        transicionar(EstadoDonacionSegmentada.VENCIDA, actor, "Donación marcada como vencida por administrador");
    }

    public List<EventoTrazabilidad> getHistorial() {
        return Collections.unmodifiableList(historial);
    }

    public EventoTrazabilidad getUltimoEvento() {
        if (historial.isEmpty())
            return null;
        return historial.get(historial.size() - 1);
    }

    private void registrarEvento(EstadoDonacionSegmentada anterior, EstadoDonacionSegmentada nuevo, String actor,
            String descripcion) {
        historial.add(new EventoTrazabilidad(anterior, nuevo, actor, descripcion));
    }
}

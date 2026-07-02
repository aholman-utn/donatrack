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
    private Long id;
    private int cantidad; //300 kg de fideos, 200lt de leche...etc
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
        if (transicionPosible(anterior, nuevoEstado)) {
            this.estado = nuevoEstado;
            registrarEvento(anterior, nuevoEstado, actor, descripcion);
        }
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
        transicionar(
                EstadoDonacionSegmentada.ENTREGADA,
                String.valueOf(entidadBeneficiariaId),
                "Entidad beneficiaria confirmó la recepción");
    }

    public void registrarEntregaFallida(String actor, String justificacion) {
        transicionar(EstadoDonacionSegmentada.ENTREGA_FALLIDA, actor, justificacion);
        // Vuelve al depósito
        transicionar(EstadoDonacionSegmentada.EN_DEPOSITO, "Sistema",
                "Donación devuelta al depósito tras entrega fallida");
    }

    public void marcarVencida(String actor) {
        // TODO: Verificar si una donacion puede marcarse como vencido (si contiene
        // bienes perdecederos)
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

    public boolean transicionPosible(EstadoDonacionSegmentada anterior, EstadoDonacionSegmentada nuevo) {
        return switch (anterior) {
            case EN_DEPOSITO ->
                nuevo == EstadoDonacionSegmentada.ASIGNACION_REALIZADA
                        || nuevo == EstadoDonacionSegmentada.VENCIDA;
            case ASIGNACION_REALIZADA -> nuevo == EstadoDonacionSegmentada.LISTA_PARA_ENTREGAR;
            case LISTA_PARA_ENTREGAR -> nuevo == EstadoDonacionSegmentada.EN_TRASLADO;
            case EN_TRASLADO ->
                nuevo == EstadoDonacionSegmentada.ENTREGADA
                        || nuevo == EstadoDonacionSegmentada.ENTREGA_FALLIDA;
            case ENTREGA_FALLIDA -> nuevo == EstadoDonacionSegmentada.EN_DEPOSITO;
            case ENTREGADA, VENCIDA -> false; // Estados finales
            default -> false;
        };
    }

    private void registrarEvento(EstadoDonacionSegmentada anterior, EstadoDonacionSegmentada nuevo, String actor,
            String descripcion) {
        historial.add(new EventoTrazabilidad(anterior, nuevo, actor, descripcion));
    }
}

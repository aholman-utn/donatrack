package com.tp.donatrack.domain.donacion;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.tp.donatrack.domain.donacion.estado.AsignacionRealizada;
import com.tp.donatrack.domain.donacion.estado.EnDeposito;
import com.tp.donatrack.domain.donacion.estado.EnTraslado;
import com.tp.donatrack.domain.donacion.estado.EntregaFallida;
import com.tp.donatrack.domain.donacion.estado.Entregada;
import com.tp.donatrack.domain.donacion.estado.ListaParaEntregar;
import com.tp.donatrack.domain.donacion.estado.Vencida;
import com.tp.donatrack.domain.donacion.exception.TransicionNoPermitidaException;
import com.tp.donatrack.domain.entidad.EntidadBeneficiaria;

import java.util.Objects;

public abstract class EstadoDonacionSegmentada {

    public static final EstadoDonacionSegmentada EN_DEPOSITO = new EnDeposito();
    public static final EstadoDonacionSegmentada ASIGNACION_REALIZADA = new AsignacionRealizada();
    public static final EstadoDonacionSegmentada LISTA_PARA_ENTREGAR = new ListaParaEntregar();
    public static final EstadoDonacionSegmentada EN_TRASLADO = new EnTraslado();
    public static final EstadoDonacionSegmentada ENTREGADA = new Entregada();
    public static final EstadoDonacionSegmentada ENTREGA_FALLIDA = new EntregaFallida();
    public static final EstadoDonacionSegmentada VENCIDA = new Vencida();

    @JsonValue
    public abstract String getNombre();

    @JsonCreator
    public static EstadoDonacionSegmentada fromString(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            return null;
        }
        return switch (nombre.trim().toUpperCase()) {
            case "EN_DEPOSITO" -> EN_DEPOSITO;
            case "ASIGNACION_REALIZADA" -> ASIGNACION_REALIZADA;
            case "LISTA_PARA_ENTREGAR" -> LISTA_PARA_ENTREGAR;
            case "EN_TRASLADO", "PENDIENTE_RECEPCION" -> EN_TRASLADO;
            case "ENTREGADA" -> ENTREGADA;
            case "ENTREGA_FALLIDA" -> ENTREGA_FALLIDA;
            case "VENCIDA" -> VENCIDA;
            default -> throw new IllegalArgumentException("Estado de donación segmentada desconocido: " + nombre);
        };
    }

    public boolean isEnDeposito() {
        return false;
    }

    public boolean isAsignada() {
        return false;
    }

    public boolean isListaParaEntregar() {
        return false;
    }

    public boolean isEnTraslado() {
        return false;
    }

    public boolean isFinalizada() {
        return false;
    }

    public boolean puedeTransicionarA(EstadoDonacionSegmentada nuevo) {
        return false;
    }

    public void asignar(DonacionSegmentada donacion, EntidadBeneficiaria entidad, String actor) {
        throw new TransicionNoPermitidaException(getNombre(), "asignar");
    }

    public void listarParaEntrega(DonacionSegmentada donacion, String actor) {
        throw new TransicionNoPermitidaException(getNombre(), "listarParaEntrega");
    }

    public void iniciarTraslado(DonacionSegmentada donacion, String actor) {
        throw new TransicionNoPermitidaException(getNombre(), "iniciarTraslado");
    }

    public void confirmarEntrega(DonacionSegmentada donacion, Long entidadBeneficiariaId) {
        throw new TransicionNoPermitidaException(getNombre(), "confirmarEntrega");
    }

    public void confirmarEntrega(DonacionSegmentada donacion, String actor) {
        Long id = null;
        try {
            if (actor != null) {
                id = Long.parseLong(actor);
            }
        } catch (NumberFormatException ignored) {
        }
        confirmarEntrega(donacion, id);
    }

    public void registrarEntregaFallida(DonacionSegmentada donacion, String actor, String justificacion) {
        throw new TransicionNoPermitidaException(getNombre(), "registrarEntregaFallida");
    }

    public void marcarVencida(DonacionSegmentada donacion, String actor) {
        throw new TransicionNoPermitidaException(getNombre(), "marcarVencida");
    }

    public void registrarLlegadaADestino(DonacionSegmentada donacion, String actor) {
        throw new TransicionNoPermitidaException(getNombre(), "registrarLlegadaADestino");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof EstadoDonacionSegmentada that)) return false;
        return Objects.equals(getNombre(), that.getNombre());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getNombre());
    }

    @Override
    public String toString() {
        return getNombre();
    }
}

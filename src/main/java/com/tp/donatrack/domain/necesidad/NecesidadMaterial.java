package com.tp.donatrack.domain.necesidad;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.tp.donatrack.domain.bien.SubCategoria;

import com.tp.donatrack.domain.donacion.DonacionSegmentada;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class NecesidadMaterial {

    private List<DonacionSegmentada> donaciones = new ArrayList<>();
    private SubCategoria subCategoria;
    private Date fechaDelPedido;
    private int cantidadObjetivo;
    private int cantidadRecibida = 0;
    private EstadoNecesidad estado;

    public NecesidadMaterial(SubCategoria subCategoria, int cantidadObjetivo, Date fechaDelPedido) {
        this.subCategoria = subCategoria;
        this.cantidadObjetivo = cantidadObjetivo;
        this.fechaDelPedido = fechaDelPedido;
        this.estado = EstadoNecesidad.ACTIVO;
    }

    public boolean activo() {
        return this.estado == EstadoNecesidad.ACTIVO;
    }

    public int cantidadFaltanteDelPedido() {
        return Math.max(cantidadObjetivo - cantidadRecibida, 0);
    }

    public void recibirDonacion(DonacionSegmentada donacion) {
        this.donaciones.add(donacion);
        recibirBienes(donacion.getCantidad());
    }

    public void recibirBienes(int cantidadRecibida) {
        this.cantidadRecibida += cantidadRecibida;
        if (cantidadFaltanteDelPedido() == 0)
            this.estado = EstadoNecesidad.SATISFECHO;
    }

    public void finalizarNecesidad() {
        if (cantidadFaltanteDelPedido() == 0) {
            this.estado = EstadoNecesidad.SATISFECHO;
        } else {
            this.estado = EstadoNecesidad.INSATISFECHO;
        }
    }
}

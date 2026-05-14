package com.tp.donatrack.domain.necesidad;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.tp.donatrack.domain.bien.Bien;
import com.tp.donatrack.domain.entidad.SubCategoria;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class NecesidadMaterial {

    private List<Bien> bienes = new ArrayList<>();
    private SubCategoria subCategoria;
    private Date fechaDelPedido;
    private int cantidad;
    private EstadoNecesidad estado;

    public NecesidadMaterial(SubCategoria subCategoria, int cantidad, Date fechaDelPedido) {
        this.subCategoria = subCategoria;
        this.cantidad = cantidad;
        this.fechaDelPedido = fechaDelPedido;
        this.estado = EstadoNecesidad.INSATISFECHO;
    }

    public int cantidadFaltanteDelPedido() {
        int bienesRecibidos = bienes.size();
        int faltante = cantidad - bienesRecibidos;
        return Math.max(faltante, 0);
    }

    public void agregarBien(Bien bien) {
        this.bienes.add(bien);
        actualizarEstado();
    }

    private void actualizarEstado() {
        int faltante = cantidadFaltanteDelPedido();
        if (faltante == 0) {
            this.estado = EstadoNecesidad.SATISFECHO;
        } else if (faltante < cantidad) {
            this.estado = EstadoNecesidad.PARCIALMENTE_SATISFECHO;
        } else {
            this.estado = EstadoNecesidad.INSATISFECHO;
        }
    }
}

package com.tp.donatrack.domain.necesidad;

import java.util.Date;

import com.tp.donatrack.domain.entidad.SubCategoria;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NecesidadRecurrente extends NecesidadMaterial {

    private String periodo;

    public NecesidadRecurrente(SubCategoria subCategoria, int cantidad, Date fechaDelPedido, String periodo) {
        super(subCategoria, cantidad, fechaDelPedido);
        this.periodo = periodo;
    }
}

package com.tp.donatrack.domain.necesidad;

import java.util.Date;

import com.tp.donatrack.domain.bien.SubCategoria;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NecesidadExtraordinaria extends NecesidadMaterial {
    private String causa;

    public NecesidadExtraordinaria(SubCategoria subCategoria, int cantidad, Date fechaDelPedido, String causa) {
        super(subCategoria, cantidad, fechaDelPedido);
        this.causa = causa;
    }

    public int adeudadas() {
        return cantidadFaltanteDelPedido();
    }
}

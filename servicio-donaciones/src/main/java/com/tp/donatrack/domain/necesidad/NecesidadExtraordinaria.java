package com.tp.donatrack.domain.necesidad;

import java.util.Date;

import com.tp.donatrack.domain.bien.SubCategoria;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "necesidades_extraordinarias")
@PrimaryKeyJoinColumn(name = "id_necesidad_material")
public class NecesidadExtraordinaria extends NecesidadMaterial {

    @Column(name = "causa")
    private String causa;

    protected NecesidadExtraordinaria() {
        super();
    }

    public NecesidadExtraordinaria(SubCategoria subCategoria, int cantidad, Date fechaDelPedido, String causa) {
        super(subCategoria, cantidad, fechaDelPedido);
        this.causa = causa;
    }

    public int adeudadas() {
        return cantidadFaltanteDelPedido();
    }
}

package com.tp.donatrack.domain.necesidad;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
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
@Table(name = "necesidades_recurrentes")
@PrimaryKeyJoinColumn(name = "id_necesidad_material")
public class NecesidadRecurrente extends NecesidadMaterial {

    @Column(name = "dias")
    private int dias;

    // Requerido por JPA.
    protected NecesidadRecurrente() {
        super();
    }

    public NecesidadRecurrente(SubCategoria subCategoria, int cantidad, Date fechaDelPedido, int dias) {
        super(subCategoria, cantidad, fechaDelPedido);
        this.dias = dias;
    }

    public boolean enPeriodo() {
        LocalDate fechaPedido = LocalDate.ofInstant(this.getFechaDelPedido().toInstant(), ZoneId.systemDefault());
        LocalDate hoy = LocalDate.now();

        long diasTranscurridos = ChronoUnit.DAYS.between(fechaPedido, hoy);

        boolean resultado = diasTranscurridos <= this.dias;
        if (!resultado) {
            finalizarNecesidad();
        }
        return resultado;
    }

    @Override
    public boolean activo() {
        if (enPeriodo()) {
            return super.activo();
        } else {
            if (super.activo())
                finalizarNecesidad();
            return false;
        }
    }
}

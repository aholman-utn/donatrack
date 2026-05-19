package com.tp.donatrack.domain.necesidad;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import com.tp.donatrack.domain.bien.SubCategoria;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NecesidadRecurrente extends NecesidadMaterial {
    private int dias;

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

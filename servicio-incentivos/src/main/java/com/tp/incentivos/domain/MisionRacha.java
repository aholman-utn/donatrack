package com.tp.incentivos.domain;

import java.time.YearMonth;

import lombok.*;

@Getter
public class MisionRacha extends Mision {

    private final int objetivo;
    private final String titulo;
    private final String descripcion;

    private int mesesConsecutivos = 0;
    private YearMonth ultimoMesRegistrado = null;

    public MisionRacha(int objetivo, String titulo, String descripcion, Insignia insignia) {
        this.objetivo = objetivo;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.insigniaAsociada = insignia;
    }

    @Override
    public boolean puedePerderProgreso() {
        return true;
    }

    @Override
    public void actualizarProgreso(InfoDonacion infoDonacion) {
        YearMonth mesDonacion = YearMonth.from(infoDonacion.getFechaDonacion());

        if (ultimoMesRegistrado == null) {
            // Primera donación registrada
            mesesConsecutivos = 1;
            ultimoMesRegistrado = mesDonacion;
        } else if (mesDonacion.equals(ultimoMesRegistrado)) {
            // nueva donación en el mismo mes: no cuenta extra
        } else if (mesDonacion.equals(ultimoMesRegistrado.plusMonths(1))) {
            // Mes siguiente al último registrado: la racha continúa
            mesesConsecutivos++;
            ultimoMesRegistrado = mesDonacion;
        } else if (mesDonacion.isAfter(ultimoMesRegistrado)) {
            // pasó más de un mes: RESET de la racha
            mesesConsecutivos = 1;
            ultimoMesRegistrado = mesDonacion;
        }
        progresoActual = mesesConsecutivos;
    }
}

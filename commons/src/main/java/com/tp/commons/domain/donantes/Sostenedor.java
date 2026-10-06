package com.tp.commons.domain.donantes;

import java.util.Optional;

public class Sostenedor extends AbstractNivel {

    @Override
    public String getNombre() {
        return "SOSTENEDOR";
    }

    @Override
    public Optional<Nivel> obtenerSiguienteNivel() {
        return Optional.of(TRANSFORMADOR);
    }
}

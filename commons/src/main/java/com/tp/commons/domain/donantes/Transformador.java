package com.tp.commons.domain.donantes;

import java.util.Optional;

public class Transformador extends AbstractNivel {

    @Override
    public String getNombre() {
        return "TRANSFORMADOR";
    }

    @Override
    public Optional<Nivel> obtenerSiguienteNivel() {
        return Optional.empty();
    }
}

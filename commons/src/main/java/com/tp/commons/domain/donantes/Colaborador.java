package com.tp.commons.domain.donantes;

import java.util.Optional;

public class Colaborador extends AbstractNivel {

    @Override
    public String getNombre() {
        return "COLABORADOR";
    }

    @Override
    public Optional<Nivel> obtenerSiguienteNivel() {
        return Optional.of(SOSTENEDOR);
    }
}

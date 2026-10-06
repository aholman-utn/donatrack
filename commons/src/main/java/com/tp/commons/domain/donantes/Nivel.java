package com.tp.commons.domain.donantes;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.List;
import java.util.Optional;

public interface Nivel {

    Nivel COLABORADOR = new Colaborador();
    Nivel SOSTENEDOR = new Sostenedor();
    Nivel TRANSFORMADOR = new Transformador();

    @JsonValue
    String getNombre();

    default String name() {
        return getNombre();
    }

    Optional<Nivel> obtenerSiguienteNivel();

    default boolean tieneSiguienteNivel() {
        return obtenerSiguienteNivel().isPresent();
    }

    static List<Nivel> values() {
        return List.of(COLABORADOR, SOSTENEDOR, TRANSFORMADOR);
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    static Nivel from(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            return COLABORADOR;
        }
        return switch (nombre.trim().toUpperCase()) {
            case "COLABORADOR" -> COLABORADOR;
            case "SOSTENEDOR" -> SOSTENEDOR;
            case "TRANSFORMADOR" -> TRANSFORMADOR;
            default -> throw new IllegalArgumentException("Nivel desconocido: " + nombre);
        };
    }
}

package com.tp.commons.domain.donantes;

import java.util.Objects;

public abstract class AbstractNivel implements Nivel {

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Nivel other)) return false;
        return getNombre() != null && getNombre().equalsIgnoreCase(other.getNombre());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getNombre() != null ? getNombre().toUpperCase() : "");
    }

    @Override
    public String toString() {
        return getNombre();
    }
}

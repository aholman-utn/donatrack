package com.tp.donatrack.domain.donacion;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Convierte {@link EstadoDonacionSegmentada} a su nombre ({@code String}) para
 * almacenarlo en una única columna y lo reconstruye al leer mediante
 * {@link EstadoDonacionSegmentada#fromString(String)}.
 */
@Converter(autoApply = false)
public class EstadoDonacionSegmentadaConverter
        implements AttributeConverter<EstadoDonacionSegmentada, String> {

    @Override
    public String convertToDatabaseColumn(EstadoDonacionSegmentada estado) {
        return estado == null ? null : estado.getNombre();
    }

    @Override
    public EstadoDonacionSegmentada convertToEntityAttribute(String nombre) {
        return EstadoDonacionSegmentada.fromString(nombre);
    }
}

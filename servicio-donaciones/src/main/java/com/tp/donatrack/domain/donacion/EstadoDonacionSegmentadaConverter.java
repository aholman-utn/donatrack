package com.tp.donatrack.domain.donacion;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Persiste el patrón State {@link EstadoDonacionSegmentada} como el nombre del
 * estado (String) en una única columna, y lo reconstruye al leer. Mantiene el
 * esquema fiel al DER (una columna de estado) sin romper el patrón State ni el
 * contrato JSON con otros servicios (se reutiliza fromString/getNombre).
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

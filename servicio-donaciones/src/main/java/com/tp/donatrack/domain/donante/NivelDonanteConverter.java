package com.tp.donatrack.domain.donante;

import com.tp.commons.domain.donantes.Nivel;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Convierte {@link Nivel} a su nombre ({@code String}) para
 * almacenarlo en una columna VARCHAR en la tabla donantes y lo reconstruye mediante {@link Nivel#from(String)}.
 */
@Converter(autoApply = false)
public class NivelDonanteConverter implements AttributeConverter<Nivel, String> {

    @Override
    public String convertToDatabaseColumn(Nivel nivel) {
        return nivel == null ? null : nivel.getNombre();
    }

    @Override
    public Nivel convertToEntityAttribute(String nombre) {
        return nombre == null ? null : Nivel.from(nombre);
    }
}

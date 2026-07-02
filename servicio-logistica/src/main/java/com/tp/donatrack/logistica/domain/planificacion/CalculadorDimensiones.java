package com.tp.donatrack.logistica.domain.planificacion;

import com.tp.commons.enums.Unidad;
import org.springframework.stereotype.Component;

@Component
public class CalculadorDimensiones {
    //Estimados
    private static final double PESO_POR_UNIDAD_KG = 5.0;
    private static final double VOLUMEN_POR_UNIDAD_M3 = 0.5;

    public DimensionesFisicas calcular(Integer cantidad, Unidad unidad) {
        if (cantidad == null || cantidad <= 0) {
            return new DimensionesFisicas(0, 0);
        }

        if (unidad == null) {
            return new DimensionesFisicas(cantidad * PESO_POR_UNIDAD_KG, cantidad * VOLUMEN_POR_UNIDAD_M3);
        }

        //Estimamos peso y volumen en base a unidad de medida de donacion
        return switch (unidad) {
            case KG -> new DimensionesFisicas(cantidad, cantidad * 0.002);
            case LITROS -> new DimensionesFisicas(cantidad, cantidad * 0.001);
            case METROS -> new DimensionesFisicas(cantidad * 2.0, cantidad * 0.1);
            case UNIDADES -> new DimensionesFisicas(cantidad * PESO_POR_UNIDAD_KG, cantidad * VOLUMEN_POR_UNIDAD_M3);
        };
    }
}
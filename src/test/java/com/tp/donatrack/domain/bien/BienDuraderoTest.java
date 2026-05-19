package com.tp.donatrack.domain.bien;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class BienDuraderoTest {

    @Test
    @DisplayName("Debe crear un Bien Duradero (Placard) con su categoría Muebles")
    public void debeCrearPlacardCorrectamente() {
        Categoria muebles = new Categoria("Muebles");
        SubCategoria placardSub = new SubCategoria(muebles, "Placard", Unidad.UNIDADES);

        BienDuradero miPlacard = new BienDuradero(
            "Placard 2 cuerpos", 
            "Madera de pino", 
            "placard.png", 
            placardSub, 
            EstadoBien.NUEVO
        );

        Assertions.assertEquals("Placard 2 cuerpos", miPlacard.getNombre());
        Assertions.assertEquals("Muebles", miPlacard.getSubCategoria().getCategoria().getDescripcion());
        Assertions.assertEquals(EstadoBien.NUEVO, miPlacard.getEstado());
    }
}
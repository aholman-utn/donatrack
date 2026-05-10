package com.tp.donatrack.domain.bien;

import com.tp.donatrack.domain.entidad.Categoria;
import com.tp.donatrack.domain.entidad.SubCategoria;
import com.tp.donatrack.domain.entidad.Unidad;
import com.tp.donatrack.domain.entidad.EstadoBien;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;

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
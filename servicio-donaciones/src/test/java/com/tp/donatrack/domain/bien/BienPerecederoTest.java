package com.tp.donatrack.domain.bien;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.Date;

public class BienPerecederoTest {

    @Test
    @DisplayName("Debe crear un Bien Perecedero (Arroz) con su categoría Alimentos")
    public void debeCrearArrozCorrectamente() {
        SubCategoria arrozSub = new SubCategoria(CategoriaBien.ALIMENTOS, "Arroz", com.tp.commons.enums.Unidad.KG);
        Date fechaVenc = new Date();

        BienPerecedero arroz = new BienPerecedero(
            "Arroz Gallo", 
            "Arroz blanco largo fino", 
            "arroz.jpg", 
            arrozSub, 
            fechaVenc
        );

        Assertions.assertEquals("Arroz Gallo", arroz.getNombre());
        Assertions.assertEquals("Arroz", arroz.getSubCategoria().getDescripcion());
    }
}
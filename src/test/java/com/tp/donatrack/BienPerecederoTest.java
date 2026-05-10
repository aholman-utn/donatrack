package com.tp.donatrack.domain.bien;

import com.tp.donatrack.domain.entidad.Categoria;
import com.tp.donatrack.domain.entidad.SubCategoria;
import com.tp.donatrack.domain.entidad.Unidad;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.Date;
import java.util.ArrayList;

public class BienPerecederoTest {

    @Test
    @DisplayName("Debe crear un Bien Perecedero (Arroz) con su categoría Alimentos")
    public void debeCrearArrozCorrectamente() {
        Categoria alimentos = new Categoria("Alimentos");
        SubCategoria arrozSub = new SubCategoria(alimentos, "Arroz", Unidad.KG);
        Date fechaVenc = new Date();

        BienPerecedero arroz = new BienPerecedero(
            "Arroz Gallo", 
            "Arroz blanco largo fino", 
            "arroz.jpg", 
            arrozSub, 
            fechaVenc
        );

        Assertions.assertEquals("Arroz Gallo", arroz.getNombre());
        Assertions.assertEquals("Alimentos", arroz.getSubCategoria().getCategoria().getDescripcion());
        Assertions.assertEquals("Arroz", arroz.getSubCategoria().getDescripcion());
    }
}
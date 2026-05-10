package com.tp.donatrack;

import com.tp.donatrack.domain.entidad.SubCategoria;
import com.tp.donatrack.domain.entidad.Categoria;
import com.tp.donatrack.domain.entidad.Unidad;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class SubCategoriaTest {

    @Test
    public void testCrearSubcategoria() {
        Categoria cat = new Categoria();
        Unidad uni = Unidad.UNIDADES;
        String desc = "Muebles";

        SubCategoria subCategoria = new SubCategoria(cat, desc, uni);

        assertNotNull(subCategoria);
        assertEquals(desc, subCategoria.getDescripcion());
        assertEquals(cat, subCategoria.getCategoria());
    }
}

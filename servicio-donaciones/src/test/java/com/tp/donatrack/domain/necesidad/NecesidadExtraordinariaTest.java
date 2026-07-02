package com.tp.donatrack.domain.necesidad;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tp.donatrack.domain.bien.CategoriaBien;
import com.tp.donatrack.domain.bien.SubCategoria;
import com.tp.commons.enums.Unidad;

class NecesidadExtraordinariaTest {

    private NecesidadExtraordinaria necesidad;
    private SubCategoria subCategoria;

    @BeforeEach
    void setUp() {
        CategoriaBien categoria = CategoriaBien.ALIMENTOS;
        subCategoria = new SubCategoria(categoria, "Alimento no perecedero", Unidad.UNIDADES);
        necesidad = new NecesidadExtraordinaria(subCategoria, 5, new Date(), "Terremoto");
    }

    @Test
    @DisplayName("Debe crear una necesidad extraordinaria con su causa")
    void crearNecesidadExtraordinariaConCausa() {
        assertEquals("Terremoto", necesidad.getCausa());
        assertEquals(5, necesidad.getCantidadObjetivo());
        assertEquals(subCategoria, necesidad.getSubCategoria());
    }

    @Test
    @DisplayName("Al agregar bienes, las adeudadas deben disminuir")
    void adeudadasDisminuyenAlAgregarBienes() {
        necesidad.recibirBienes(1);
        assertEquals(4, necesidad.adeudadas());
    }

    @Test
    @DisplayName("Al satisfacer la necesidad, las adeudadas deben ser 0")
    void adeudadasCeroAlSatisfacer() {
        necesidad.recibirBienes(5);
        assertEquals(0, necesidad.adeudadas());
        assertEquals(EstadoNecesidad.SATISFECHO, necesidad.getEstado());
    }
}

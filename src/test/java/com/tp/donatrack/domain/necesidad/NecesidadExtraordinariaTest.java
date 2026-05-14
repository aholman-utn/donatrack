package com.tp.donatrack.domain.necesidad;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tp.donatrack.domain.bien.BienDuradero;
import com.tp.donatrack.domain.entidad.Categoria;
import com.tp.donatrack.domain.entidad.EstadoBien;
import com.tp.donatrack.domain.entidad.SubCategoria;
import com.tp.donatrack.domain.entidad.Unidad;

class NecesidadExtraordinariaTest {

    private NecesidadExtraordinaria necesidad;
    private SubCategoria subCategoria;

    @BeforeEach
    void setUp() {
        Categoria categoria = new Categoria("Alimentos");
        subCategoria = new SubCategoria(categoria, "Alimento no perecedero", Unidad.UNIDADES);
        necesidad = new NecesidadExtraordinaria(subCategoria, 5, new Date(), "Terremoto");
    }

    @Test
    @DisplayName("Debe crear una necesidad extraordinaria con su causa")
    void crearNecesidadExtraordinariaConCausa() {
        assertEquals("Terremoto", necesidad.getCausa());
        assertEquals(5, necesidad.getCantidad());
        assertEquals(subCategoria, necesidad.getSubCategoria());
    }

    @Test
    @DisplayName("Las adeudadas deben ser igual a la cantidad faltante")
    void adeudadasIgualACantidadFaltante() {
        assertEquals(5, necesidad.adeudadas());
    }

    @Test
    @DisplayName("Al agregar bienes, las adeudadas deben disminuir")
    void adeudadasDisminuyenAlAgregarBienes() {
        BienDuradero bien = new BienDuradero("Arroz", "Arroz blanco", "arroz.png", subCategoria, EstadoBien.NUEVO);
        necesidad.agregarBien(bien);

        assertEquals(4, necesidad.adeudadas());
    }

    @Test
    @DisplayName("Al satisfacer la necesidad, las adeudadas deben ser 0")
    void adeudadasCeroAlSatisfacer() {
        for (int i = 0; i < 5; i++) {
            BienDuradero bien = new BienDuradero("Bien " + i, "Desc", "img.png", subCategoria, EstadoBien.NUEVO);
            necesidad.agregarBien(bien);
        }

        assertEquals(0, necesidad.adeudadas());
        assertEquals(EstadoNecesidad.SATISFECHO, necesidad.getEstado());
    }
}

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

class NecesidadMaterialTest {

    private NecesidadExtraordinaria necesidad;
    private SubCategoria subCategoria;
    private Categoria categoria;

    @BeforeEach
    void setUp() {
        categoria = new Categoria("Muebles");
        subCategoria = new SubCategoria(categoria, "Sillas", Unidad.UNIDADES);
        necesidad = new NecesidadExtraordinaria(subCategoria, 3, new Date(), "Inundación");
    }

    @Test
    @DisplayName("Una necesidad nueva debe tener estado INSATISFECHO")
    void necesidadNuevaDebeEstarInsatisfecha() {
        assertEquals(EstadoNecesidad.INSATISFECHO, necesidad.getEstado());
    }

    @Test
    @DisplayName("La cantidad faltante inicial debe ser igual a la cantidad pedida")
    void cantidadFaltanteInicialIgualACantidad() {
        assertEquals(3, necesidad.cantidadFaltanteDelPedido());
    }

    @Test
    @DisplayName("Al agregar un bien, la cantidad faltante debe disminuir")
    void alAgregarBienDisminuyeFaltante() {
        BienDuradero silla = new BienDuradero("Silla", "Silla de madera", "silla.png", subCategoria, EstadoBien.NUEVO);
        necesidad.agregarBien(silla);

        assertEquals(2, necesidad.cantidadFaltanteDelPedido());
    }

    @Test
    @DisplayName("Al agregar un bien parcialmente, el estado debe ser PARCIALMENTE_SATISFECHO")
    void estadoParcialmenteSatisfecho() {
        BienDuradero silla = new BienDuradero("Silla", "Silla de madera", "silla.png", subCategoria, EstadoBien.NUEVO);
        necesidad.agregarBien(silla);

        assertEquals(EstadoNecesidad.PARCIALMENTE_SATISFECHO, necesidad.getEstado());
    }

    @Test
    @DisplayName("Al completar todos los bienes, el estado debe ser SATISFECHO")
    void estadoSatisfechoAlCompletarBienes() {
        for (int i = 0; i < 3; i++) {
            BienDuradero silla = new BienDuradero("Silla " + i, "Silla de madera", "silla.png", subCategoria, EstadoBien.NUEVO);
            necesidad.agregarBien(silla);
        }

        assertEquals(EstadoNecesidad.SATISFECHO, necesidad.getEstado());
        assertEquals(0, necesidad.cantidadFaltanteDelPedido());
    }

    @Test
    @DisplayName("La cantidad faltante nunca debe ser negativa")
    void cantidadFaltanteNuncaNegativa() {
        for (int i = 0; i < 5; i++) {
            BienDuradero silla = new BienDuradero("Silla " + i, "Silla", "silla.png", subCategoria, EstadoBien.NUEVO);
            necesidad.agregarBien(silla);
        }

        assertEquals(0, necesidad.cantidadFaltanteDelPedido());
    }
}

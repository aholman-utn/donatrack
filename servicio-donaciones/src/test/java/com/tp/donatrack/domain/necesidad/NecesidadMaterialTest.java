package com.tp.donatrack.domain.necesidad;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

import com.tp.donatrack.domain.bien.*;
import com.tp.donatrack.domain.donacion.DonacionSegmentada;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class NecesidadMaterialTest {

    private NecesidadExtraordinaria necesidad;
    private SubCategoria subCategoria;
    private CategoriaBien categoria;
    private DonacionSegmentada donacion;

    @BeforeEach
    void setUp() {
        categoria = CategoriaBien.MOBILIARIO;
        subCategoria = new SubCategoria(categoria, "Sillas", com.tp.commons.enums.Unidad.UNIDADES);
        necesidad = new NecesidadExtraordinaria(subCategoria, 3, new Date(), "Inundación");
    }

    @Test
    @DisplayName("Una necesidad nueva debe tener estado ACTIVO")
    void necesidadNuevaDebeEstarInsatisfecha() {
        assertEquals(EstadoNecesidad.ACTIVO, necesidad.getEstado());
    }

    @Test
    @DisplayName("La cantidad faltante inicial debe ser igual a la cantidad pedida")
    void cantidadFaltanteInicialIgualACantidad() {
        assertEquals(3, necesidad.cantidadFaltanteDelPedido());
    }

    @Test
    @DisplayName("Al agregar una donacion, la cantidad faltante debe disminuir")
    void alAgregarBienDisminuyeFaltante() {
        donacion = new DonacionSegmentada(1, subCategoria, null);
        necesidad.recibirDonacion(donacion);
        assertEquals(2, necesidad.cantidadFaltanteDelPedido());
    }

    @Test
    @DisplayName("Al agregar una donacion parcialmente, el estado debe ser ACTIVO")
    void estadoParcialmenteSatisfecho() {
        donacion = new DonacionSegmentada(1, subCategoria, null);
        necesidad.recibirDonacion(donacion);
        assertEquals(EstadoNecesidad.ACTIVO, necesidad.getEstado());
    }

    @Test
    @DisplayName("Al completar todos los bienes, el estado debe ser SATISFECHO")
    void estadoSatisfechoAlCompletarBienes() {
        donacion = new DonacionSegmentada(3, subCategoria, null);
        necesidad.recibirDonacion(donacion);
        assertEquals(EstadoNecesidad.SATISFECHO, necesidad.getEstado());
        assertEquals(0, necesidad.cantidadFaltanteDelPedido());
    }

    @Test
    @DisplayName("La cantidad faltante nunca debe ser negativa")
    void cantidadFaltanteNuncaNegativa() {
        donacion = new DonacionSegmentada(5, subCategoria, null);
        necesidad.recibirDonacion(donacion);
        assertEquals(0, necesidad.cantidadFaltanteDelPedido());
    }
}

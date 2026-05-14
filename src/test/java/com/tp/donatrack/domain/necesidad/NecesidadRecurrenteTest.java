package com.tp.donatrack.domain.necesidad;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tp.donatrack.domain.entidad.Categoria;
import com.tp.donatrack.domain.entidad.SubCategoria;
import com.tp.donatrack.domain.entidad.Unidad;

class NecesidadRecurrenteTest {

    private NecesidadRecurrente necesidad;
    private SubCategoria subCategoria;

    @BeforeEach
    void setUp() {
        Categoria categoria = new Categoria("Higiene");
        subCategoria = new SubCategoria(categoria, "Productos de limpieza", Unidad.UNIDADES);
        necesidad = new NecesidadRecurrente(subCategoria, 10, new Date(), "Mensual");
    }

    @Test
    @DisplayName("Debe crear una necesidad recurrente con su periodo")
    void crearNecesidadRecurrenteConPeriodo() {
        assertEquals("Mensual", necesidad.getPeriodo());
        assertEquals(10, necesidad.getCantidad());
        assertEquals(subCategoria, necesidad.getSubCategoria());
    }

    @Test
    @DisplayName("Una necesidad recurrente nueva debe estar INSATISFECHA")
    void necesidadRecurrenteNuevaInsatisfecha() {
        assertEquals(EstadoNecesidad.INSATISFECHO, necesidad.getEstado());
    }

    @Test
    @DisplayName("Se puede modificar el periodo de una necesidad recurrente")
    void modificarPeriodo() {
        necesidad.setPeriodo("Semanal");
        assertEquals("Semanal", necesidad.getPeriodo());
    }

    @Test
    @DisplayName("La cantidad faltante debe ser igual a la cantidad pedida inicialmente")
    void cantidadFaltanteInicial() {
        assertEquals(10, necesidad.cantidadFaltanteDelPedido());
    }
}

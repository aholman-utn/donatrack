package com.tp.donatrack.domain.necesidad;

import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tp.donatrack.domain.bien.Categoria;
import com.tp.donatrack.domain.bien.SubCategoria;
import com.tp.donatrack.domain.bien.Unidad;

class NecesidadRecurrenteTest {

    private NecesidadRecurrente necesidad;
    private SubCategoria subCategoria;

    @BeforeEach
    void setUp() {
        Categoria categoria = Categoria.HIGIENE;
        subCategoria = new SubCategoria(categoria, "Productos de limpieza", Unidad.UNIDADES);
        necesidad = new NecesidadRecurrente(subCategoria, 10, new Date(), 30);
    }

    @Test
    @DisplayName("Debe crear una necesidad recurrente con su periodo")
    void crearNecesidadRecurrenteConPeriodo() {
        assertEquals(30, necesidad.getDias());
        assertEquals(10, necesidad.getCantidadObjetivo());
        assertEquals(subCategoria, necesidad.getSubCategoria());
    }

    @Test
    @DisplayName("Una necesidad recurrente nueva debe estar ACTIVA")
    void necesidadRecurrenteNuevaActiva() {
        assertEquals(EstadoNecesidad.ACTIVO, necesidad.getEstado());
        assertTrue(necesidad.activo());
    }

    @Test
    @DisplayName("Se puede modificar el periodo de una necesidad recurrente")
    void modificarPeriodo() {
        necesidad.setDias(7);
        assertEquals(7, necesidad.getDias());
    }

    @Test
    @DisplayName("La cantidad faltante debe ser igual a la cantidad pedida inicialmente")
    void cantidadFaltanteInicial() {
        assertEquals(10, necesidad.cantidadFaltanteDelPedido());
    }

    @Test
    @DisplayName("Si el periodo finaliza, la nececidad deja de estar activa")
    void periodoFonaliza() {
        Calendar cal = Calendar.getInstance();
        cal.set(2026, Calendar.MAY, 15);
        necesidad.setFechaDelPedido(cal.getTime());
        necesidad.setDias(2);
        assertFalse(necesidad.enPeriodo());
        assertFalse(necesidad.activo());
    }
}

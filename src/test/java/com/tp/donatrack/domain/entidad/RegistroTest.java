package com.tp.donatrack.domain.entidad;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RegistroTest {

    @Test
    @DisplayName("Debe crear un registro con su descripción")
    void crearRegistroConDescripcion() {
        Registro registro = new Registro("Donación recibida el 10/05/2026");
        assertEquals("Donación recibida el 10/05/2026", registro.getDescripcion());
    }

    @Test
    @DisplayName("Se puede modificar la descripción de un registro")
    void modificarDescripcion() {
        Registro registro = new Registro("Descripción original");
        registro.setDescripcion("Descripción actualizada");
        assertEquals("Descripción actualizada", registro.getDescripcion());
    }
}

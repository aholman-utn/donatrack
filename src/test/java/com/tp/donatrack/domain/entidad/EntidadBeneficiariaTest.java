package com.tp.donatrack.domain.entidad;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

import com.tp.donatrack.domain.bien.Categoria;
import com.tp.donatrack.domain.bien.SubCategoria;
import com.tp.donatrack.domain.bien.Unidad;
import com.tp.donatrack.domain.donacion.DonacionSegmentada;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tp.donatrack.domain.necesidad.NecesidadExtraordinaria;
import com.tp.donatrack.domain.necesidad.NecesidadRecurrente;

class EntidadBeneficiariaTest {

    private EntidadBeneficiaria entidad;
    private SubCategoria subCategoria;
    private DonacionSegmentada donacionSegmentada;

    @BeforeEach
    void setUp() {
        entidad = new EntidadBeneficiaria();
        Categoria categoria = new Categoria("Alimentos");
        subCategoria = new SubCategoria(categoria, "Alimento no perecedero", Unidad.UNIDADES);
        donacionSegmentada = new DonacionSegmentada(1, subCategoria, null);
    }

    @Test
    @DisplayName("Una entidad beneficiaria nueva no tiene necesidades materiales")
    void entidadNuevaSinNecesidades() {
        assertEquals(0, entidad.getCantNecesidades());
    }

    @Test
    @DisplayName("Una entidad beneficiaria nueva no tiene necesidades materiales activas")
    void entidadNuevaSinNecesidadesActivas() {
        assertEquals(0, entidad.getCantNececidadesActivas());
    }

    @Test
    @DisplayName("Se puede agregar una necesidad extraordinaria a la entidad")
    void agregarNecesidadExtraordinaria() {
        NecesidadExtraordinaria necesidad = new NecesidadExtraordinaria(subCategoria, 5, new Date(), "Incendio");
        entidad.agregarNecesidad(necesidad);

        assertEquals(1, entidad.getCantNecesidades());
    }

    @Test
    @DisplayName("Se puede agregar una necesidad recurrente a la entidad")
    void agregarNecesidadRecurrente() {
        NecesidadRecurrente necesidad = new NecesidadRecurrente(subCategoria, 10, new Date(), 30);
        entidad.agregarNecesidad(necesidad);

        assertEquals(1, entidad.getCantNecesidades());
    }

    @Test
    @DisplayName("Se pueden agregar múltiples necesidades a la entidad")
    void agregarMultiplesNecesidades() {
        NecesidadExtraordinaria extraordinaria = new NecesidadExtraordinaria(subCategoria, 5, new Date(), "Inundación");
        NecesidadRecurrente recurrente = new NecesidadRecurrente(subCategoria, 10, new Date(), 7);

        entidad.agregarNecesidad(extraordinaria);
        entidad.agregarNecesidad(recurrente);

        assertEquals(2, entidad.getCantNecesidades());
    }

    @Test
    @DisplayName("Se puede remover una necesidad de la entidad")
    void removerNecesidad() {
        NecesidadExtraordinaria necesidad = new NecesidadExtraordinaria(subCategoria, 5, new Date(), "Incendio");
        entidad.agregarNecesidad(necesidad);
        entidad.removerNecesidad(necesidad);

        assertEquals(0, entidad.getCantNecesidades());
    }

    @Test
    @DisplayName("Al agregar una nececidad la cantidad de nececidades activas debe aumentar")
    void necesidadesActivas() {
        NecesidadExtraordinaria extraordinaria = new NecesidadExtraordinaria(subCategoria, 5, new Date(), "Inundación");
        entidad.agregarNecesidad(extraordinaria);
        assertEquals(1, entidad.getCantNececidadesActivas());
    }

    @Test
    @DisplayName("Al agregar una nececidad la cantidad de nececidades activas debe ser igual a la cantidad de nececidades")
    void conNecesidadesActivas() {
        NecesidadExtraordinaria extraordinaria = new NecesidadExtraordinaria(subCategoria, 1, new Date(), "Inundación");
        entidad.agregarNecesidad(extraordinaria);
        assertEquals(entidad.getCantNecesidades(), entidad.getCantNececidadesActivas());
    }

    @Test
    @DisplayName("Si no existe una nececidad, se lanza una exepcion al implementar una doncacion")
    void exepcionImplementarDoncacion() {
        assertThrows(RuntimeException.class, () -> {entidad.implementarDonacion(donacionSegmentada);});
    }

    @Test
    @DisplayName("Al implementar una donacion, es posible satisfacer una nececidad")
    void satisfacerNececidad() {
        NecesidadRecurrente necesidad = new NecesidadRecurrente(subCategoria, 1, new Date(), 1);
        entidad.agregarNecesidad(necesidad);
        entidad.implementarDonacion(donacionSegmentada);
        assertEquals(0, entidad.getCantNececidadesActivas());
    }

    @Test
    @DisplayName("Al implementar una donacion, es posible que no se complete una nececidad")
    void noSatisfacerNececidad() {
        NecesidadRecurrente necesidad = new NecesidadRecurrente(subCategoria, 2, new Date(), 1);
        entidad.agregarNecesidad(necesidad);
        entidad.implementarDonacion(donacionSegmentada);
        assertEquals(1, entidad.getCantNececidadesActivas());
    }
}

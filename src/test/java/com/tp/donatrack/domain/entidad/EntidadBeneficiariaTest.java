package com.tp.donatrack.domain.entidad;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tp.donatrack.domain.necesidad.NecesidadExtraordinaria;
import com.tp.donatrack.domain.necesidad.NecesidadMaterial;
import com.tp.donatrack.domain.necesidad.NecesidadRecurrente;

class EntidadBeneficiariaTest {

    private EntidadBeneficiaria entidad;
    private SubCategoria subCategoria;

    @BeforeEach
    void setUp() {
        entidad = new EntidadBeneficiaria();
        Categoria categoria = new Categoria("Alimentos");
        subCategoria = new SubCategoria(categoria, "Alimento no perecedero", Unidad.UNIDADES);
    }

    @Test
    @DisplayName("Una entidad beneficiaria nueva no tiene necesidades materiales")
    void entidadNuevaSinNecesidades() {
        assertEquals(0, entidad.getCantNecesidadesMateriales());
    }

    @Test
    @DisplayName("Se puede agregar una necesidad extraordinaria a la entidad")
    void agregarNecesidadExtraordinaria() {
        NecesidadExtraordinaria necesidad = new NecesidadExtraordinaria(subCategoria, 5, new Date(), "Incendio");
        entidad.agregarNecesidad(necesidad);

        assertEquals(1, entidad.getCantNecesidadesMateriales());
    }

    @Test
    @DisplayName("Se puede agregar una necesidad recurrente a la entidad")
    void agregarNecesidadRecurrente() {
        NecesidadRecurrente necesidad = new NecesidadRecurrente(subCategoria, 10, new Date(), "Mensual");
        entidad.agregarNecesidad(necesidad);

        assertEquals(1, entidad.getCantNecesidadesMateriales());
    }

    @Test
    @DisplayName("Se pueden agregar múltiples necesidades a la entidad")
    void agregarMultiplesNecesidades() {
        NecesidadExtraordinaria extraordinaria = new NecesidadExtraordinaria(subCategoria, 5, new Date(), "Inundación");
        NecesidadRecurrente recurrente = new NecesidadRecurrente(subCategoria, 10, new Date(), "Semanal");

        entidad.agregarNecesidad(extraordinaria);
        entidad.agregarNecesidad(recurrente);

        assertEquals(2, entidad.getCantNecesidadesMateriales());
    }

    @Test
    @DisplayName("Se puede remover una necesidad de la entidad")
    void removerNecesidad() {
        NecesidadExtraordinaria necesidad = new NecesidadExtraordinaria(subCategoria, 5, new Date(), "Incendio");
        entidad.agregarNecesidad(necesidad);
        entidad.removerNecesidad(necesidad);

        assertEquals(0, entidad.getCantNecesidadesMateriales());
    }
}

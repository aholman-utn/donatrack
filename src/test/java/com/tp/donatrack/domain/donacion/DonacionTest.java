package com.tp.donatrack.domain.donacion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.tp.donatrack.domain.bien.Bien;
import com.tp.donatrack.domain.donante.Donante;
import com.tp.donatrack.domain.bien.SubCategoria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

class DonacionTest {
    private Donacion donacion;
    private Donante donanteMock;
    private SubCategoria perecederos;
    private SubCategoria duraderos;
    private Bien manzana;
    private Bien leche;
    private Bien silla;

    @BeforeEach
    void setUp() {
        donanteMock = mock(Donante.class);
        perecederos = mock(SubCategoria.class);
        duraderos = mock(SubCategoria.class);

        manzana = crearBienMock(perecederos);
        leche = crearBienMock(perecederos);
        silla = crearBienMock(duraderos);

        List<Bien> bienesEntrantes = Arrays.asList(manzana, leche, silla);

        donacion = new Donacion(
            donanteMock, 
            "Donación de campaña invernal", 
            new Date(),
            bienesEntrantes
        );
    }

    @Test
    @DisplayName("Debería segmentar bienes perecederos y duraderos en segmentos separados")
    void testSegmentarDonacion() {
        List<DonacionSegmentada> resultados = donacion.getDonacionesSegmentadas();

        assertThat(resultados).hasSize(2);

        DonacionSegmentada segPerecederos = donacion.buscarPorSubcategoria(perecederos).get();
        assertThat(segPerecederos.getCantidad()).isEqualTo(2);
        assertThat(segPerecederos.getBienes()).containsExactlyInAnyOrder(manzana, leche);

        DonacionSegmentada segDuraderos = donacion.buscarPorSubcategoria(duraderos).get();
        assertThat(segDuraderos.getCantidad()).isEqualTo(1);
        assertThat(segDuraderos.getBienes()).contains(silla);
        
        assertThat(donacion.getDonante()).isEqualTo(donanteMock);
    }

    @Test
    @DisplayName("Debería mantener la integridad de la descripción y fecha")
    void testAtributosDonacion() {
        assertThat(donacion.getDescripcion()).isEqualTo("Donación de campaña invernal");
        assertThat(donacion.getFechaIngreso()).isBeforeOrEqualTo(new Date());
    }

    private Bien crearBienMock(SubCategoria sub) {
        Bien bien = mock(Bien.class);
        when(bien.getSubCategoria()).thenReturn(sub);
        return bien;
    }
}
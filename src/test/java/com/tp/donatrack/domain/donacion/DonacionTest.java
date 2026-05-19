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

    @BeforeEach
    void setUp() {
        donanteMock = mock(Donante.class);
        perecederos = mock(SubCategoria.class);
        duraderos = mock(SubCategoria.class);

        donacion = new Donacion(
            donanteMock, 
            "Donación de campaña invernal", 
            new Date()
        );
    }

    @Test
    @DisplayName("Debería segmentar bienes perecederos y duraderos en segmentos separados")
    void testSegmentarDonacion() {
        Bien manzana = crearBienMock(perecederos);
        Bien leche = crearBienMock(perecederos);
        Bien silla = crearBienMock(duraderos);

        List<Bien> bienesEntrantes = Arrays.asList(manzana, leche, silla);

        donacion.segmentar(bienesEntrantes);

        List<DonacionSegmentada> resultados = donacion.getDonacionesSegmentadas();

        assertThat(resultados).hasSize(2);

        DonacionSegmentada segPerecederos = buscarPorSubcategoria(resultados, perecederos);
        assertThat(segPerecederos.getCantidad()).isEqualTo(2);
        assertThat(segPerecederos.getBienes()).containsExactlyInAnyOrder(manzana, leche);

        DonacionSegmentada segDuraderos = buscarPorSubcategoria(resultados, duraderos);
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

    private DonacionSegmentada buscarPorSubcategoria(List<DonacionSegmentada> lista, SubCategoria sub) {
        return lista.stream()
                .filter(s -> s.getSubCategoria().equals(sub))
                .findFirst()
                .orElseThrow(() -> new AssertionError("No se encontró el segmento para la subcategoría"));
    }
}
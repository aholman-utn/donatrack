package com.tp.donatrack.domain.donante;

import com.tp.donatrack.domain.bien.CategoriaBien;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import java.time.LocalDate;


class PerfilDonanteTest {

    private PerfilDonante perfil;

    @BeforeEach
    void setUp() {
        perfil = new PerfilDonante();
    }

    @Test
    void testRachaAumentaSiSonConsecutivos() {
        LocalDate hoy = LocalDate.now();

        perfil.getHistorialDonaciones().add(
                ItemDonacionSegmentada.builder().fecha(hoy.minusMonths(1)).build()
        );

        perfil.getHistorialDonaciones().add(
                ItemDonacionSegmentada.builder().fecha(hoy).build()
        );

        int racha = perfil.calcularRachaMeses();

        assertEquals(2, racha, "La racha debería ser 2 en meses consecutivos");
    }

    @Test
    void testRachaSeRompeSiHayUnMesDeSalto() {
        LocalDate hoy = LocalDate.now();

        perfil.getHistorialDonaciones().add(
                ItemDonacionSegmentada.builder().fecha(hoy.minusMonths(3)).build()
        );

        perfil.getHistorialDonaciones().add(
                ItemDonacionSegmentada.builder().fecha(hoy.minusMonths(1)).build()
        );

        int racha = perfil.calcularRachaMeses();

        assertEquals(1, racha, "Al saltar un mes, la racha debe resetearse a 1");
    }

    @Test
    void testRachaDonacionMesesConsecutivos() {
        LocalDate hoy = LocalDate.now();
        // Enero y Febrero
        perfil.getHistorialDonaciones().add(ItemDonacionSegmentada.builder().fecha(hoy.minusMonths(1)).build());
        perfil.getHistorialDonaciones().add(ItemDonacionSegmentada.builder().fecha(hoy).build());

        assertEquals(2, perfil.calcularRachaMeses(), "La racha debería ser 2");
    }

    @Test
    void testCompletitudCategoriasDistintas() {
        perfil.getHistorialDonaciones().add(ItemDonacionSegmentada.builder().categoria(CategoriaBien.ALIMENTOS).build());
        perfil.getHistorialDonaciones().add(ItemDonacionSegmentada.builder().categoria(CategoriaBien.MOBILIARIO).build());
        perfil.getHistorialDonaciones().add(ItemDonacionSegmentada.builder().categoria(CategoriaBien.ALIMENTOS).build());

        assertEquals(2, perfil.contarCategoriasUnicas(), "Debería contar solo 2 categorías únicas");
    }

    @Test
    void testDonacionesExitosasTotales() {
        perfil.getMetricasPerfil().setTotalDonacionesExitosas(0);

        perfil.getMetricasPerfil().setTotalDonacionesExitosas(5);

        assertEquals(5, perfil.getMetricasPerfil().getTotalDonacionesExitosas());
    }

    @Test
    void testCantidadDonacionesEntregadas() {
        perfil.getHistorialDonaciones().add(ItemDonacionSegmentada.builder().build());
        perfil.getHistorialDonaciones().add(ItemDonacionSegmentada.builder().build());

        assertEquals(2, perfil.calcularCantidadDonacionesEntregadas());
    }
}
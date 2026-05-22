package com.tp.donatrack.domain.donacion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.stream.Collectors;

import com.tp.donatrack.domain.bien.EstadoBien;
import com.tp.donatrack.domain.bien.Bien;
import com.tp.donatrack.domain.bien.BienPerecedero;
import com.tp.donatrack.domain.bien.BienDuradero;
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
    private BienPerecedero manzana;
    private BienPerecedero leche;
    private BienDuradero silla;

    @BeforeEach
    void setUp() {
        donanteMock = mock(Donante.class);
        perecederos = mock(SubCategoria.class);
        duraderos = mock(SubCategoria.class);

        Date fechaVencimientoComun = new Date();

        manzana = new BienPerecedero(
            "Manzana", 
            "Cajón de manzanas", 
            "url_foto_manzana", 
            perecederos, 
            fechaVencimientoComun
        );
        
        leche = new BienPerecedero(
            "Leche", 
            "Sachet de leche entera", 
            "url_foto_leche", 
            perecederos, 
            fechaVencimientoComun
        );
        
        silla = new BienDuradero(
            "Silla", 
            "Silla de oficina", 
            "url_foto_silla", 
            duraderos, 
            EstadoBien.NUEVO 
        );

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

    @Test
    @DisplayName("Debería retornar PENDIENTE como estado inicial cuando se crea la donación")
    void testEstadoInicialEsPendiente() {
        assertThat(donacion.getEstado()).isEqualTo(EstadoDonacion.PENDIENTE);
    }

    @Test
    @DisplayName("Debería seguir retornando PENDIENTE si solo algunos segmentos fueron adjudicados")
    void testEstadoCuandoSeAdjudicaParcialmente() {
        DonacionSegmentada segPerecederos = donacion.buscarPorSubcategoria(perecederos).get();
        segPerecederos.setEstado(EstadoDonacionSegmentada.ADJUDICADA);

        assertThat(donacion.getEstado()).isEqualTo(EstadoDonacion.PENDIENTE);
    }

    @Test
    @DisplayName("Debería cambiar el estado a ADJUDICADA cuando todos los segmentos individuales estén adjudicados")
    void testEstadoCuandoSeAdjudicanTodosLosSegmentos() {
        List<DonacionSegmentada> todosLosSegmentos = donacion.getDonacionesSegmentadas();

        for (DonacionSegmentada segmento : todosLosSegmentos) {
            segmento.setEstado(EstadoDonacionSegmentada.ADJUDICADA);
        }

        assertThat(donacion.getEstado()).isEqualTo(EstadoDonacion.ADJUDICADA);
    }

    @Test
    @DisplayName("Debe agrupar por misma subcategoría y separar si difieren en criterio (fecha/estado)")
    void segmentar_separaPorCriterioPolimorfico() {
        Donante donanteMock = mock(Donante.class);
        SubCategoria alimentos = mock(SubCategoria.class);
        SubCategoria muebles = mock(SubCategoria.class);

        Date venceHoy = new Date();
        Date venceEnUnMes = new Date(venceHoy.getTime() + (1000L * 60 * 60 * 24 * 30));

        BienPerecedero leche = new BienPerecedero("Leche", "Sachet", "leche.jpg", alimentos, venceHoy);
        BienPerecedero yogur = new BienPerecedero("Yogur", "Pote", "yogurt.jpg", alimentos, venceHoy);
        BienPerecedero arroz = new BienPerecedero("Arroz", "Paquete", "arroz.jpg", alimentos, venceEnUnMes);

        BienDuradero sillaNueva = new BienDuradero("Silla", "De madera", "silla.jpg", muebles, EstadoBien.NUEVO);
        BienDuradero mesaUsada = new BienDuradero("Mesa", "De pino", "mesa.jpg", muebles, EstadoBien.USADO);

        List<Bien> bienesEntrantes = Arrays.asList(leche, yogur, arroz, sillaNueva, mesaUsada);

        Donacion donacion = new Donacion(donanteMock, "Donación Mixta", new Date(), bienesEntrantes);
        List<DonacionSegmentada> segmentosGenerados = donacion.getDonacionesSegmentadas();

        assertEquals(4, segmentosGenerados.size(), "Debería haber generado 4 segmentos distintos en total");

        List<DonacionSegmentada> segmentosAlimentos = segmentosGenerados.stream()
                .filter(s -> s.getSubCategoria().equals(alimentos))
                .collect(Collectors.toList());

        assertEquals(2, segmentosAlimentos.size(), "Alimentos debe partirse en 2 donaciones por tener fechas distintas");

        DonacionSegmentada donacionParaHoy = segmentosAlimentos.stream()
                .filter(s -> s.getBienes().size() == 2)
                .findFirst()
                .orElseThrow();
        
        assertTrue(donacionParaHoy.getBienes().contains(leche), "Debe contener la leche");
        assertTrue(donacionParaHoy.getBienes().contains(yogur), "Debe contener el yogur");

        DonacionSegmentada donacionParaElMesQueViene = segmentosAlimentos.stream()
                .filter(s -> s.getBienes().size() == 1)
                .findFirst()
                .orElseThrow();
                
        assertTrue(donacionParaElMesQueViene.getBienes().contains(arroz), "El arroz debe estar en un segmento solo");

        List<DonacionSegmentada> segmentosMuebles = segmentosGenerados.stream()
                .filter(s -> s.getSubCategoria().equals(muebles))
                .collect(Collectors.toList());

        assertEquals(2, segmentosMuebles.size(), "Muebles debe partirse en 2 donaciones por tener estados distintos");
        
        boolean existeSillaSola = segmentosMuebles.stream().anyMatch(s -> s.getBienes().contains(sillaNueva));
        boolean existeMesaSola = segmentosMuebles.stream().anyMatch(s -> s.getBienes().contains(mesaUsada));
        
        assertTrue(existeSillaSola, "La silla nueva debe estar en su propia caja");
        assertTrue(existeMesaSola, "La mesa usada debe estar en su propia caja");
    }
}
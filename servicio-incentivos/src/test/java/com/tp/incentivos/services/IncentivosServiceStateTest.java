package com.tp.incentivos.services;

import com.tp.commons.domain.donantes.Nivel;
import com.tp.commons.domain.notificador.TipoNotificador;
import com.tp.commons.dtos.incentivos.EvaluacionMisionResponseDTO;
import com.tp.commons.dtos.incentivos.IndicadoresDonanteDTO;
import com.tp.commons.dtos.notificador.NotificacionRequestDTO;
import com.tp.commons.services.notificador.NotificacionQueueClient;
import com.tp.incentivos.clients.DonacionesRestClient;
import com.tp.incentivos.clients.InsigniasRestClient;
import com.tp.incentivos.domain.misiones.*;
import com.tp.incentivos.dtos.EntregaDonacionDTO;
import com.tp.incentivos.repositories.MisionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class IncentivosServiceStateTest {

    private IncentivosService incentivosService;
    private MisionRepository misionRepository;

    @BeforeEach
    public void setUp() {
        List<Mision> catalogo = new ArrayList<>();

        Mision colExitosas = new MisionDonacionesExitosas(1, "Donacion Exitosa", "Lograr 1 donación.");
        colExitosas.setNivel(Nivel.COLABORADOR); colExitosas.setOrden(1);
        Mision colHabil = new MisionHabilDonador(5, "Habil Donador", "Donación > 5 bienes.");
        colHabil.setNivel(Nivel.COLABORADOR); colHabil.setOrden(2);
        Mision colCompletitud = new MisionCompletitud(2, "Completitud", "2 categorías distintas.");
        colCompletitud.setNivel(Nivel.COLABORADOR); colCompletitud.setOrden(3);

        Mision sosExitosas = new MisionDonacionesExitosas(5, "Donacion Exitosa", "Lograr 5 donaciones.");
        sosExitosas.setNivel(Nivel.SOSTENEDOR); sosExitosas.setOrden(1);
        Mision sosCompletitud = new MisionCompletitud(4, "Completitud", "4 categorías distintas.");
        sosCompletitud.setNivel(Nivel.SOSTENEDOR); sosCompletitud.setOrden(2);
        Mision sosHabil = new MisionHabilDonador(10, "Habil Donador", "Donación > 10 bienes.");
        sosHabil.setNivel(Nivel.SOSTENEDOR); sosHabil.setOrden(3);

        Mision traRacha = new MisionRacha(3, "Racha", "3 meses consecutivos.");
        traRacha.setNivel(Nivel.TRANSFORMADOR); traRacha.setOrden(1);
        Mision traExitosas = new MisionDonacionesExitosas(15, "Donacion Exitosa", "Lograr 15 donaciones.");
        traExitosas.setNivel(Nivel.TRANSFORMADOR); traExitosas.setOrden(2);
        Mision traCompletitud = new MisionCompletitud(5, "Completitud", "5 categorías distintas.");
        traCompletitud.setNivel(Nivel.TRANSFORMADOR); traCompletitud.setOrden(3);
        Mision traHabil = new MisionHabilDonador(20, "Habil Donador", "Donación > 20 bienes.");
        traHabil.setNivel(Nivel.TRANSFORMADOR); traHabil.setOrden(4);

        catalogo.addAll(List.of(
                colExitosas, colHabil, colCompletitud,
                sosExitosas, sosCompletitud, sosHabil,
                traRacha, traExitosas, traCompletitud, traHabil
        ));

        misionRepository = (MisionRepository) Proxy.newProxyInstance(
                MisionRepository.class.getClassLoader(),
                new Class<?>[]{MisionRepository.class},
                (proxy, method, args) -> {
                    if (method.isDefault()) {
                        return InvocationHandler.invokeDefault(proxy, method, args);
                    }
                    if ("findByNivelAndOrden".equals(method.getName())) {
                        Nivel n = (Nivel) args[0];
                        int ord = (int) args[1];
                        return catalogo.stream()
                                .filter(m -> m.getNivel() == n && m.getOrden() == ord)
                                .findFirst();
                    }
                    if ("findByNivelAndId".equals(method.getName())) {
                        Nivel n = (Nivel) args[0];
                        Long id = (Long) args[1];
                        return catalogo.stream()
                                .filter(m -> m.getNivel() == n && m.getId().equals(id))
                                .findFirst();
                    }
                    return null;
                }
        );

        DonacionesRestClient donacionesStub = new DonacionesRestClient() {
            @Override
            public IndicadoresDonanteDTO obtenerIndicadores(Long donanteId, Long donacionSegmentadaId, List<String> indicadores) {
                return IndicadoresDonanteDTO.builder()
                        .cantidadBienesTotal(100)
                        .mesesConsecutivosRacha(12)
                        .cantidadCategoriasUnicas(10)
                        .cantidadDonacionesEntregadas(100)
                        .build();
            }

            @Override
            public NotificacionRequestDTO obtenerDatosParaNotificar(Long donanteId) {
                return null;
            }
        };

        InsigniasRestClient insigniasStub = new InsigniasRestClient(null, "http://dummy") {
            @Override
            public void notificarInsigniaObtenida(String donante, String titulo, String descripcion) {
            }
        };

        NotificacionQueueClient notificacionStub = new NotificacionQueueClient(null) {
            @Override
            public boolean notificar(TipoNotificador tipo, String destinatario, String mensaje, String asunto, Long personaId) {
                return true;
            }
        };

        incentivosService = new IncentivosService(misionRepository, donacionesStub, insigniasStub, notificacionStub);
    }

    @Test
    @DisplayName("Donante COLABORADOR al completar la última misión asciende polimórficamente a SOSTENEDOR")
    public void testColaboradorAsciendeASostenedor() {
        EntregaDonacionDTO dto = new EntregaDonacionDTO();
        dto.setDonanteId(1L);
        dto.setDonacionSegmentadaId(10L);
        dto.setCategoriaDonante(Nivel.COLABORADOR);
        dto.setUltimaMisionId(3L);
        dto.setNombreDonante("Juan Perez");
        dto.setProgreso(0.0);

        EvaluacionMisionResponseDTO response = incentivosService.procesarNuevaEntrega(dto);

        assertTrue(response.isMisionCumplida());
        assertTrue(response.isSubioDeCategoria(), "Debe subir de categoría al completar la última misión de su nivel");
        assertEquals(Nivel.SOSTENEDOR, response.getNuevoNivel(), "El nuevo nivel debe ser SOSTENEDOR");
        assertEquals(1L, response.getSiguienteMisionId(), "Al subir de nivel debe reiniciar en la misión inicial (ID 1)");
    }

    @Test
    @DisplayName("Donante SOSTENEDOR al completar la última misión asciende polimórficamente a TRANSFORMADOR")
    public void testSostenedorAsciendeATransformador() {
        EntregaDonacionDTO dto = new EntregaDonacionDTO();
        dto.setDonanteId(2L);
        dto.setDonacionSegmentadaId(20L);
        dto.setCategoriaDonante(Nivel.SOSTENEDOR);
        dto.setUltimaMisionId(3L);
        dto.setNombreDonante("Maria Gomez");
        dto.setProgreso(0.0);

        EvaluacionMisionResponseDTO response = incentivosService.procesarNuevaEntrega(dto);

        assertTrue(response.isMisionCumplida());
        assertTrue(response.isSubioDeCategoria());
        assertEquals(Nivel.TRANSFORMADOR, response.getNuevoNivel());
        assertEquals(1L, response.getSiguienteMisionId());
    }

    @Test
    @DisplayName("Donante TRANSFORMADOR al completar la última misión no tiene siguiente nivel (techo de progresión)")
    public void testTransformadorNoAsciende() {
        EntregaDonacionDTO dto = new EntregaDonacionDTO();
        dto.setDonanteId(3L);
        dto.setDonacionSegmentadaId(30L);
        dto.setCategoriaDonante(Nivel.TRANSFORMADOR);
        dto.setUltimaMisionId(4L);
        dto.setNombreDonante("Carlos Tevez");
        dto.setProgreso(0.0);

        EvaluacionMisionResponseDTO response = incentivosService.procesarNuevaEntrega(dto);

        assertTrue(response.isMisionCumplida());
        assertFalse(response.isSubioDeCategoria(), "No debe subir de categoría porque ya es TRANSFORMADOR");
        assertEquals(Nivel.TRANSFORMADOR, response.getNuevoNivel(), "Mantiene nivel TRANSFORMADOR");
    }
}

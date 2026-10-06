package com.tp.incentivos.services;

import com.tp.commons.domain.donantes.Nivel;
import com.tp.commons.domain.notificador.TipoNotificador;
import com.tp.commons.dtos.incentivos.EvaluacionMisionResponseDTO;
import com.tp.commons.dtos.incentivos.IndicadoresDonanteDTO;
import com.tp.commons.dtos.notificador.NotificacionRequestDTO;
import com.tp.commons.services.notificador.NotificacionQueueClient;
import com.tp.incentivos.clients.DonacionesRestClient;
import com.tp.incentivos.clients.InsigniasRestClient;
import com.tp.incentivos.dtos.EntregaDonacionDTO;
import com.tp.incentivos.repositories.MisionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class IncentivosServiceStateTest {

    private IncentivosService incentivosService;
    private MisionRepository misionRepository;

    @BeforeEach
    public void setUp() {
        misionRepository = new MisionRepository();

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
                return null; // Evita despacho de notificaciones
            }
        };

        InsigniasRestClient insigniasStub = new InsigniasRestClient(null, "http://dummy") {
            @Override
            public void notificarInsigniaObtenida(String donante, String titulo, String descripcion) {
                // No-op
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
        // En COLABORADOR, la última misión es la ID 3 (MisionCompletitud)
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
        // En SOSTENEDOR, la última misión es la ID 3 (MisionHabilDonador)
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
        // En TRANSFORMADOR, la última misión es la ID 4 (MisionHabilDonador)
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
        assertNull(response.getSiguienteMisionId(), "No hay más misiones siguientes al completar el nivel máximo");
    }
}

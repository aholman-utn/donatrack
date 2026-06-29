package com.tp.incentivos.services;

import com.tp.commons.dtos.incentivos.EvaluacionMisionResponseDTO;
import com.tp.commons.dtos.incentivos.IndicadoresDonanteDTO;
import com.tp.incentivos.clients.DonacionesRestClient;
import com.tp.incentivos.domain.misiones.Mision;
import com.tp.incentivos.dtos.*;
import com.tp.incentivos.repositories.MisionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.Optional;
import java.util.List;

@Service
public class IncentivosService {
    private final MisionRepository misionRepository;
    private final DonacionesRestClient donacionesRestClient;
    private static final Logger logger = LoggerFactory.getLogger(IncentivosService.class);

    public IncentivosService(
            MisionRepository misionRepository,
            DonacionesRestClient donacionesRestClient
    ) {
        this.misionRepository = misionRepository;
        this.donacionesRestClient = donacionesRestClient;
    }

    public EvaluacionMisionResponseDTO procesarNuevaEntrega(EntregaDonacionDTO dto) {
        logger.info("Iniciando procesamiento de entrega. Donante: {}, Misión original: {}",
                dto.getDonanteId(), dto.getUltimaMisionId());

        Long ultimaMisionId = (dto.getUltimaMisionId() == null) ? obtenerMisionInicialId() : dto.getUltimaMisionId();
        logger.debug("Misión resuelta para procesar: {}", ultimaMisionId);

        Mision misionActual = this.misionRepository.findById(ultimaMisionId)
                .orElseThrow(() -> {
                    logger.error("Error crítico: Misión con ID {} no encontrada en base de datos", ultimaMisionId);
                    return new RuntimeException("Misión no encontrada con ID: " + ultimaMisionId);
                });

        logger.debug("Llamando a DonacionesClient para obtener indicadores...");
        IndicadoresDonanteDTO indicadores = this.donacionesRestClient.obtenerIndicadores(
                dto.getDonanteId(),
                dto.getDonacionSegmentadaId(),
                List.of(
                    "CANTIDAD_BIENES",
                    "MESES_CONSECUTIVOS",
                    "CATEGORIAS_UNICAS",
                    "ENTREGAS_EXITOSAS_TOTALES"
                )
        );
        logger.debug("Indicadores recibidos: {}", indicadores);

        boolean cumplida = misionActual.estaCumplida(dto, indicadores);
        logger.info("Resultado de evaluación de misión (Cumplida: {}): {} para donante {}",
                cumplida, misionActual.getTitulo(), dto.getDonanteId());

        if (cumplida) {
            Optional<Mision> siguienteMision = this.misionRepository.findSiguiente(ultimaMisionId);
            Long siguienteMisionId = siguienteMision.map(Mision::getId).orElse(null);

            logger.info("Misión completada. Siguiente misión ID: {}", siguienteMisionId);

            //TODO activar n8n

            return EvaluacionMisionResponseDTO.builder()
                    .misionCumplida(true)
                    .nuevoProgreso(0.0)
                    .insigniaGanada(misionActual.getInsigniaAsociada())
                    .siguienteMisionId(siguienteMisionId)
                    .build();
        }

        double progresoActualizado = misionActual.calcularNuevoProgreso(dto, indicadores);
        logger.info("Misión no completada. Progreso actualizado a: {}", progresoActualizado);

        return EvaluacionMisionResponseDTO.builder()
                .misionCumplida(false)
                .nuevoProgreso(progresoActualizado)
                .insigniaGanada(null)
                .siguienteMisionId(ultimaMisionId)
                .build();
    }

    //TODO Debatir: Deberiamos crearle la mision inicial al usuario en donaciones ? deberiamos manejarlo desde aca pero de otra forma?
    private Long obtenerMisionInicialId() {
        return 1L;
    }
}
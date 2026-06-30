package com.tp.incentivos.services;

import com.tp.commons.domain.donantes.Nivel;
import com.tp.commons.dtos.incentivos.EvaluacionMisionResponseDTO;
import com.tp.commons.dtos.incentivos.IndicadoresDonanteDTO;
import com.tp.commons.dtos.notificador.NotificacionRequestDTO;
import com.tp.commons.services.notificador.NotificacionRestClient;
import com.tp.incentivos.clients.DonacionesRestClient;
import com.tp.incentivos.clients.InsigniasRestClient;
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
    private final InsigniasRestClient insigniasRestClient;
    private final NotificacionRestClient notificacionRestClient;
    private static final Logger logger = LoggerFactory.getLogger(IncentivosService.class);

    public IncentivosService(
            MisionRepository misionRepository,
            DonacionesRestClient donacionesRestClient,
            InsigniasRestClient insigniasRestClient,
            NotificacionRestClient notificacionRestClient
    ) {
        this.misionRepository = misionRepository;
        this.donacionesRestClient = donacionesRestClient;
        this.insigniasRestClient = insigniasRestClient;
        this.notificacionRestClient = notificacionRestClient;
    }

    public EvaluacionMisionResponseDTO procesarNuevaEntrega(EntregaDonacionDTO dto) {
        logger.info("Iniciando procesamiento de entrega. Donante: {}, Misión original: {}",
                dto.getDonanteId(), dto.getUltimaMisionId());

        Long ultimaMisionId = (dto.getUltimaMisionId() == null) ? obtenerMisionInicialId() : dto.getUltimaMisionId();
        logger.info("Misión resuelta para procesar: {}", ultimaMisionId);

        Mision misionActual = this.misionRepository.findById(ultimaMisionId)
            .orElseThrow(() -> {
                logger.error("Error crítico: Misión con ID {} no encontrada en base de datos", ultimaMisionId);
                return new RuntimeException("Misión no encontrada con ID: " + ultimaMisionId);
            });

        logger.info("Llamando a DonacionesClient para obtener indicadores...");
        IndicadoresDonanteDTO indicadores = this.donacionesRestClient.obtenerIndicadores(
                dto.getDonanteId(),
                dto.getDonacionSegmentadaId(),
                List.of(
                    "CANTIDAD_BIENES",
                    "MESES_CONSECUTIVOS",
                    "CATEGORIAS_DISTINTAS",
                    "ENTREGAS_EXITOSAS_TOTALES"
                )
        );

        boolean cumplida = misionActual.estaCumplida(dto, indicadores);
        logger.info("Resultado de evaluación de misión (Cumplida: {}): {} para donante {}",
                cumplida, misionActual.getTitulo(), dto.getDonanteId());

        if (cumplida) {
            Optional<Mision> siguienteMision = this.misionRepository.findSiguiente(ultimaMisionId);
            Long siguienteMisionId = siguienteMision.map(Mision::getId).orElse(null);

            logger.info("Misión completada. Siguiente misión ID: {}", siguienteMisionId);

            boolean subioDeCategoria = false;
            Nivel nuevoNivel = dto.getCategoriaDonante();

            if (siguienteMisionId == null) {
                if (nuevoNivel == Nivel.COLABORADOR) {
                    nuevoNivel = Nivel.SOSTENEDOR;
                } else if (nuevoNivel == Nivel.SOSTENEDOR) {
                    nuevoNivel = Nivel.TRANSFORMADOR;
                }
                subioDeCategoria = true;
            }

            this.insigniasRestClient.notificarInsigniaObtenida(
                dto.getNombreDonante(),
                misionActual.getTitulo(),
                misionActual.getDescripcion()
            );

            //Notificamos al usuario que completo la mision.
            this.notificarMisionCumplida(dto.getDonanteId(), misionActual.getTitulo());

            if (subioDeCategoria) {
                //Notificamos al usuario que subio de categoria.
                this.notificarSubidaNivel(dto.getDonanteId(), nuevoNivel);
            }

            return EvaluacionMisionResponseDTO.builder()
                    .misionCumplida(true)
                    .nuevoProgreso(0.0)
                    .insigniaGanada(misionActual.getInsigniaAsociada())
                    .siguienteMisionId(siguienteMisionId)
                    .subioDeCategoria(subioDeCategoria)
                    .nuevoNivel(nuevoNivel)
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

    public void notificarMisionCumplida(Long donanteId, String tituloMision) {
        String mensaje = "¡Felicitaciones! Has completado con éxito la misión: " + tituloMision;
        String asunto = "¡Nueva Insignia Desbloqueada!";

        this.despacharNotificacion(donanteId, mensaje, asunto);
    }

    public void notificarSubidaNivel(Long donanteId, Nivel nuevoNivel) {
        String mensaje = "¡Increíble! Gracias a tu compromiso constante, has ascendido a la categoría: " + nuevoNivel;
        String asunto = "¡Subiste de Nivel en DonaTrack!";

        this.despacharNotificacion(donanteId, mensaje, asunto);
    }

    private void despacharNotificacion(Long donanteId, String mensaje, String asunto) {
        try {
            NotificacionRequestDTO requestNotificacion = this.donacionesRestClient.obtenerDatosParaNotificar(donanteId);

            if (requestNotificacion == null || requestNotificacion.getMedio() == null || requestNotificacion.getDestinatario() == null) {
                logger.warn("Donante ID {} sin medio configurado. No se despacha la notificación: {}", donanteId, asunto);
                return;
            }

            requestNotificacion.setMensaje(mensaje);
            requestNotificacion.setAsunto(asunto);

            logger.info("Enviando request de notificación ({}) para donante {} vía {}", asunto, donanteId, requestNotificacion.getMedio());

            this.notificacionRestClient.notificar(
                    requestNotificacion.getMedio(),
                    requestNotificacion.getDestinatario(),
                    requestNotificacion.getMensaje(),
                    requestNotificacion.getAsunto(),
                    donanteId
            );

            logger.info("Notificación '{}' despachada con éxito.", asunto);

        } catch (IllegalArgumentException e) {
            logger.error("ERROR: El tipo de medio no es válido para el donante {}.", donanteId, e);
        } catch (Exception e) {
            logger.error("ERROR INESPERADO al notificar al donante {}.", donanteId, e);
        }
    }

    //TODO Debatir: Deberiamos crearle la mision inicial al usuario en donaciones ? deberiamos manejarlo desde aca pero de otra forma?
    private Long obtenerMisionInicialId() {
        return 1L;
    }
}
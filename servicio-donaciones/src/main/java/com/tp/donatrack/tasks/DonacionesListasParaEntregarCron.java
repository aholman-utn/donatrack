package com.tp.donatrack.tasks;
import com.tp.donatrack.clients.LogisticaQueueClient;
import com.tp.donatrack.domain.donacion.DonacionSegmentada;
import com.tp.donatrack.domain.donacion.EstadoDonacionSegmentada;
import com.tp.donatrack.domain.entidad.EntidadBeneficiaria;
import com.tp.commons.dtos.logistica.DonacionSegmentadaListaParaEntregarALogisticaDTO;
import com.tp.donatrack.services.DonacionService;
import com.tp.donatrack.services.EntidadBeneficiariaService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class DonacionesListasParaEntregarCron {
    private static final Logger logger = LoggerFactory.getLogger(DonacionesListasParaEntregarCron.class);

    private final EntidadBeneficiariaService entidadBeneficiariaService;
    private final DonacionService donacionService;
    private final LogisticaQueueClient logisticaQueueClient;

    public DonacionesListasParaEntregarCron(
            DonacionService donacionService,
            EntidadBeneficiariaService entidadBeneficiariaService,
            LogisticaQueueClient logisticaQueueClient
    ) {
        this.donacionService = donacionService;
        this.entidadBeneficiariaService = entidadBeneficiariaService;
        this.logisticaQueueClient = logisticaQueueClient;
    }

    // Se ejecuta todos los días a las 8:00 AM.
    // La expresión se externaliza para poder acelerarla en pruebas sin tocar el código:
    //   $env:DONATRACK_CRON_DONACIONESLISTAS = "0 * * * * *"
    @Scheduled(cron = "${donatrack.cron.donaciones-listas:0 0 8 * * *}")
    public void enviarDonacionesListasParaEntregar() {
        logger.info("Iniciando procesamiento de cron para donaciones listas para entregar...");

        List<DonacionSegmentada> donacionesListasParaEntregar =
                donacionService.obtenerDonacionesSegmentadas(EstadoDonacionSegmentada.LISTA_PARA_ENTREGAR, 100);

        if (donacionesListasParaEntregar.isEmpty()) {
            logger.info("No se encontraron donaciones listas para entregar.");
            return;
        }

        Set<Long> idsEntidades = donacionesListasParaEntregar.stream()
                .map(DonacionSegmentada::getEntidadBeneficiariaAsignadaId)
                .collect(Collectors.toSet());

        List<EntidadBeneficiaria> entidades = entidadBeneficiariaService.listarPorIds(idsEntidades);

        Map<Long, EntidadBeneficiaria> mapaEntidades = entidades.stream()
                .filter(entidad -> entidad.getDatosDeEntidad() != null)
                .collect(Collectors.toMap(
                        entidad -> entidad.getDatosDeEntidad().getId(),
                        entidad -> entidad
                ));


        // Solo se envían las que tienen una dirección de entrega resoluble.
        // Se separa el filtro del mapeo para poder transicionar exactamente las mismas
        // que se enviaron: marcar una que no viajó la dejaría trabada en EN_PLANIFICACION.
        List<DonacionSegmentada> segmentadasAEnviar = donacionesListasParaEntregar.stream()
                .filter(segmentada -> {
                    EntidadBeneficiaria entidad = mapaEntidades.get(segmentada.getEntidadBeneficiariaAsignadaId());

                    if (
                        entidad == null ||
                        entidad.getDatosDeEntidad() == null ||
                        entidad.getDatosDeEntidad().getDireccion() == null
                    ) {
                        return false;
                    }

                    String direccionTexto = entidad.getDatosDeEntidad().getDireccion().getDireccion();

                    return !direccionTexto.isEmpty();
                })
                .toList();

        int descartadas = donacionesListasParaEntregar.size() - segmentadasAEnviar.size();
        if (descartadas > 0) {
            logger.warn("Se descartaron {} donaciones segmentadas por no tener dirección de entrega. "
                    + "Quedan en LISTA_PARA_ENTREGAR para el próximo ciclo.", descartadas);
        }

        if (segmentadasAEnviar.isEmpty()) {
            logger.info("Ninguna donación tiene dirección de entrega válida. No se envía lote.");
            return;
        }

        List<DonacionSegmentadaListaParaEntregarALogisticaDTO> bodyEnvio = segmentadasAEnviar.stream()
                .map(segmentada -> {
                    EntidadBeneficiaria entidad = mapaEntidades.get(segmentada.getEntidadBeneficiariaAsignadaId());

                    String direccionTexto = entidad.getDatosDeEntidad().getDireccion().getDireccion();

                    return new DonacionSegmentadaListaParaEntregarALogisticaDTO(
                            segmentada.getId(),
                            segmentada.getEntidadBeneficiariaAsignadaId(),
                            direccionTexto,
                            segmentada.getBienes().size(),
                            segmentada.getSubCategoria().getUnidad()
                    );
                })
                .toList();
        try {
            this.logisticaQueueClient.enviarLoteDonaciones(bodyEnvio);
            logger.info("Se enviaron exitosamente {} donaciones.", bodyEnvio.size());

            // Solo se transicionan las que efectivamente se enviaron.
            // Si el publish falla, no se ejecuta y quedan disponibles para el próximo ciclo.
            for (DonacionSegmentada segmentada : segmentadasAEnviar) {
                segmentada.solicitarPlanificacion("Sistema (Cron)");
            }
        } catch (Exception e) {
            logger.error("Error al enviar lote de donaciones segmentadas: {}", e.getMessage(), e);
        }

        logger.info("Fin del procesamiento del cron..");
    }
}
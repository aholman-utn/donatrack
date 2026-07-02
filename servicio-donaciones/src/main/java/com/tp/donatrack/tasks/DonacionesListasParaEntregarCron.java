package com.tp.donatrack.tasks;
import com.tp.donatrack.clients.LogisticaRestClient;
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
    private final LogisticaRestClient logisticaRestClient;

    public DonacionesListasParaEntregarCron(
            DonacionService donacionService,
            EntidadBeneficiariaService entidadBeneficiariaService,
            LogisticaRestClient logisticaRestClient
    ) {
        this.donacionService = donacionService;
        this.entidadBeneficiariaService = entidadBeneficiariaService;
        this.logisticaRestClient = logisticaRestClient;
    }

    // Se ejecuta todos los días a las 8:00 AM
    @Scheduled(cron = "0 * * * * *")
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


        List<DonacionSegmentadaListaParaEntregarALogisticaDTO> bodyEnvio = donacionesListasParaEntregar.stream()
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
            this.logisticaRestClient.enviarLoteDonaciones(bodyEnvio);
            logger.info("Se enviaron exitosamente {} donaciones.", bodyEnvio.size());
        } catch (Exception e) {
            logger.error("Error al enviar lote de donaciones segmentadas: {}", e.getMessage(), e);
        }

        logger.info("Fin del procesamiento del cron..");
    }
}
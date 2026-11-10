package com.tp.donatrack.tasks;

import com.tp.commons.services.notificador.NotificacionQueueClient;
import com.tp.donatrack.dtos.DonanteInactivoDTO;
import com.tp.donatrack.services.DonanteService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.util.List;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class DonantesInactivosCron {
    private static final Logger logger = LoggerFactory.getLogger(DonantesInactivosCron.class);
    private final NotificacionQueueClient notificacionQueueClient;
    private final DonanteService donanteService; 

    public DonantesInactivosCron(
            NotificacionQueueClient notificacionQueueClient,
        DonanteService donanteService
    ) {
        this.notificacionQueueClient = notificacionQueueClient;
        this.donanteService = donanteService;
    }

    // Se ejecuta todos los días a las 8:00 AM
    @Scheduled(cron = "${donatrack.cron.donantes-inactivos:0 0 8 * * *}", zone = "America/Argentina/Buenos_Aires")
    public void enviarNotificacionDonantesInactivos() {
        logger.info("Iniciando procesamiento de cron para donantes inactivos...");

        List<DonanteInactivoDTO> donantesInactivos = donanteService.obtenerDonantesSinInteraccionMasDeDias(20);

        donantesInactivos.forEach(donante -> {
            LocalDateTime fecha = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
            if (!donanteService.reservarAvisoInactividad(donante, fecha)) return;
            try {
                String asunto = "¡Te extrañamos en Donatrack!";
                String mensaje = "Hace un tiempo que no registrás actividad. Tu ayuda es muy importante, ¡sumate con una nueva donación!";

                boolean enviado = notificacionQueueClient.notificar(
                        donante.getTipoNotificadorPreferido(),
                        donante.getContacto(),
                        mensaje,
                        asunto,
                        donante.getId()
                );
                if (!enviado) donanteService.liberarAvisoInactividad(donante.getId(), fecha);
            } catch (Exception e) {
                donanteService.liberarAvisoInactividad(donante.getId(), fecha);
                logger.error("Error al notificar inactividad para el donante {}", donante.getId(), e);
            }
        });
        
        logger.info("Fin del procesamiento del cron..");
    }
}
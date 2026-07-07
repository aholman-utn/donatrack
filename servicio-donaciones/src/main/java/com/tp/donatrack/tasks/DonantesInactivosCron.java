package com.tp.donatrack.tasks;

import com.tp.commons.services.notificador.NotificacionQueueClient;
import com.tp.donatrack.dtos.DonanteInactivoDTO;
import com.tp.donatrack.services.DonanteService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.util.List;
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
    @Scheduled(cron = "0 * * * * *")
    public void enviarNotificacionDonantesInactivos() {
        logger.info("Iniciando procesamiento de cron para donantes inactivos...");

        List<DonanteInactivoDTO> donantesInactivos = donanteService.obtenerDonantesSinInteraccionMasDeDias(20);

        donantesInactivos.forEach(donante -> {
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

            } catch (Exception e) {
                System.err.println("Error al notificar al donante " + e.getMessage());
            }
        });
        
        logger.info("Fin del procesamiento del cron..");
    }
}
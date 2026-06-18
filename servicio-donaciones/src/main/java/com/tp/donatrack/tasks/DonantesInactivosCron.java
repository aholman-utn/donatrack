package com.tp.donatrack.tasks;

import com.tp.donatrack.dtos.DonanteInactivoDTO;
import com.tp.donatrack.domain.notificacion.Notificacion;
import com.tp.donatrack.domain.notificacion.TipoNotificacion;
import com.tp.donatrack.services.NotificacionService;
import com.tp.donatrack.services.DonanteService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class DonantesInactivosCron {

    private final NotificacionService notificacionService;
    private final DonanteService donanteService; 

    public DonantesInactivosCron(
        NotificacionService notificacionService, 
        DonanteService donanteService
    ) {
        this.notificacionService = notificacionService;
        this.donanteService = donanteService;
    }

    // Se ejecuta todos los días a las 8:00 AM
    @Scheduled(cron = "0 0 8 * * *")
    public void enviarNotificacionDonantesInactivos() {
        System.out.println("Iniciando procesamiento de cron para donantes inactivos...");

        List<DonanteInactivoDTO> donantesInactivos = donanteService.obtenerDonantesSinInteraccionMasDeDias(20);

        donantesInactivos.forEach(donante -> {
            try {
                Notificacion notificacion = new Notificacion(
                    "¡Te extrañamos en Donatrack!",
                    "Hace un tiempo que no registrás actividad. Tu ayuda es muy importante, ¡sumate con una nueva donación!",
                    "Inactividad",
                    TipoNotificacion.INACTIVIDAD
                );
                //Post de notificaciones.
                notificacionService.notificar(
                    notificacion,
                    donante.getTipoNotificadorPreferido(), 
                    donante.getContacto()
                );
                
                donanteService.guardarNotificacionEnHistorial(donante.getId(), notificacion);
            
            } catch (Exception e) {
                System.err.println("Error al notificar al donante " + e.getMessage());
            }
        });
        
        System.out.println("Fin del procesamiento del cron.");
    }
}
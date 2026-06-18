package com.tp.donatrack.services;

import com.tp.donatrack.domain.notificacion.Notificacion;
import com.tp.donatrack.domain.notificador.TipoNotificador;
import com.tp.donatrack.dtos.NotificacionRequestDTO;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class NotificacionService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String urlServicioNotificaciones = "http://localhost:8082/api/notificaciones";

    public void notificar(Notificacion notif, TipoNotificador tipo, String contacto) {
        
        NotificacionRequestDTO dto = new NotificacionRequestDTO();
        dto.setTitulo(notif.getTitulo());
        dto.setCuerpo(notif.getCuerpo());
        dto.setAsunto(notif.getAsunto());
        
        dto.setTipo(tipo); 
        
        dto.setContacto(contacto);

        try {
            restTemplate.postForEntity(urlServicioNotificaciones, dto, Void.class);
            System.out.println("POST enviado con éxito al servicio de notificaciones.");
        } catch (Exception e) {
            System.err.println("Falló la comunicación con el servicio de notificaciones: " + e.getMessage());
        }
    }
}
package com.tp.donatrack.notificaciones.repositories;

import com.tp.donatrack.notificaciones.domain.entities.Notificacion;

import com.tp.donatrack.notificaciones.dtos.NotificacionInputDTO;
import org.springframework.stereotype.Repository;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Repository
public class NotificacionRepository {

    List<Notificacion> notificaciones;
    Long id;
    public NotificacionRepository(){
        this.notificaciones = new ArrayList();
        this.id=Long.valueOf(0);
    }

    public Notificacion save(NotificacionInputDTO notif){
        Notificacion notif_creada = new Notificacion();
        notif_creada.setId(this.id+1);
        notif_creada.setTitulo(notif.getTitulo());
        notif_creada.setCuerpo(notif.getCuerpo());
        notif_creada.setFecha(LocalDateTime.now());
        notif_creada.setId_persona(notif.getId_persona());
        notificaciones.add(notif_creada);
        return notif_creada;
    }
}

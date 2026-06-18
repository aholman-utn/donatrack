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

    public Notificacion save(Long persona_id, String titulo, String mensaje){
        Notificacion notif_creada = new Notificacion();
        notif_creada.setId(this.id+1);
        notif_creada.setTitulo(titulo);
        notif_creada.setCuerpo(mensaje);
        notif_creada.setFecha(LocalDateTime.now());
        notif_creada.setId_persona(persona_id);
        notificaciones.add(notif_creada);
        return notif_creada;
    }

    public List<Notificacion> findByIdPersona(Long id){
        return this.notificaciones.stream().filter(notif -> notif.getId_persona().equals(id)).toList();
    }

    public List<Notificacion> findAll(){
        return this.notificaciones;
    }
}

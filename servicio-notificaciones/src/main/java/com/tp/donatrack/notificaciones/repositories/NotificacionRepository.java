package com.tp.donatrack.notificaciones.repositories;

import com.tp.donatrack.notificaciones.domain.entities.Notificacion;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Repository
public class NotificacionRepository {

    private final List<Notificacion> notificaciones;
    private Long idSequence; //Id incrementable

    public NotificacionRepository() {
        this.notificaciones = new ArrayList<>();
        this.idSequence = 0L;
    }

    public Notificacion save(Notificacion notif) {
        this.idSequence++;
        notif.setId(this.idSequence);

        notificaciones.add(notif);
        return notif;
    }

    public List<Notificacion> findByIdPersona(Long idPersona) {
        return this.notificaciones.stream()
                .filter(notif -> notif.getId_persona() != null && notif.getId_persona().equals(idPersona))
                .toList();
    }

    public List<Notificacion> findAll() {
        return new ArrayList<>(this.notificaciones);
    }
}
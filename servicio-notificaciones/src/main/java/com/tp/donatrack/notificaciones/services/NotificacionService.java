package com.tp.donatrack.notificaciones.services;


import com.tp.donatrack.notificaciones.domain.entities.MedioNotificador;
import com.tp.donatrack.notificaciones.domain.entities.Notificacion;
import com.tp.donatrack.notificaciones.domain.entities.iNotificador;

import com.tp.donatrack.notificaciones.dtos.NotificacionInputDTO;
import com.tp.donatrack.notificaciones.dtos.NotificacionOutputDTO;
import com.tp.donatrack.notificaciones.dtos.NotificarServicioExterno;
import com.tp.donatrack.notificaciones.repositories.NotificacionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificacionService {

    NotificacionRepository notificacionRepository;
    List<iNotificador> notificadores;
    public NotificacionService(NotificacionRepository repo){
        this.notificacionRepository = repo;
    }

    public void notificar(NotificarServicioExterno body){
        iNotificador notificador = this.seleccionarNotificador(body.getMedio());
        if(notificador!= null) {
            notificador.enviarNotificacion(body.getDestinatario(), body.getMensaje());
        }
    }

    public iNotificador seleccionarNotificador(MedioNotificador medio){
        return this.notificadores.stream().filter(notificador -> notificador.getMedio().equals(medio)).findFirst().orElse(null);
    }

    public Notificacion crearNotificacion(Long persona_id, String titulo, String mensaje){
        return this.notificacionRepository.save(persona_id, titulo, mensaje);
    }

    public List<Notificacion> buscar(Long id_persona){
        return this.notificacionRepository.findByIdPersona(id_persona);
    }

    public List<Notificacion> buscarTodas(){
        return this.notificacionRepository.findAll();
    }
}

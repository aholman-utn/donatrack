package com.tp.donatrack.notificaciones.controllers;


import com.tp.donatrack.notificaciones.domain.entities.Notificacion;

import com.tp.donatrack.notificaciones.dtos.NotificacionInputDTO;
import com.tp.donatrack.notificaciones.dtos.NotificacionOutputDTO;
import com.tp.donatrack.notificaciones.dtos.NotificarServicioExterno;
import com.tp.donatrack.notificaciones.services.NotificacionService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notificaciones")
public class NotificacionController {

    private final NotificacionService notifService;

    public NotificacionController(NotificacionService service){
        this.notifService=service;
    }

    @PostMapping("/notificar")
    public void notificar(@Valid @RequestBody NotificarServicioExterno body){
        this.notifService.notificar(body);
    }

    @PostMapping("/")
    public NotificacionOutputDTO crearNotificacion(@Valid @RequestBody NotificacionInputDTO body){
        Notificacion notif_creada = this.notifService.crearNotificacion(body);
        NotificacionOutputDTO response = new NotificacionOutputDTO();
        response.setId(notif_creada.getId());
        response.setTitulo(notif_creada.getTitulo());
        response.setFecha(notif_creada.getFecha());
        response.setId_persona(notif_creada.getId_persona());
        response.setCuerpo(notif_creada.getCuerpo());
        return response;
    }
}

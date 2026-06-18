package com.tp.donatrack.notificaciones.controllers;


import com.tp.donatrack.notificaciones.domain.entities.Notificacion;

import com.tp.donatrack.notificaciones.dtos.NotificacionInputDTO;
import com.tp.donatrack.notificaciones.dtos.NotificacionOutputDTO;
import com.tp.donatrack.notificaciones.dtos.NotificarServicioExterno;
import com.tp.donatrack.notificaciones.services.NotificacionService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notificaciones")
public class NotificacionController {

    private final NotificacionService notifService;

    public NotificacionController(NotificacionService service){
        this.notifService=service;
    }

    @GetMapping("/")
    public List<NotificacionOutputDTO> buscarTodas(@Valid @PathVariable Long idPersona){
        List<Notificacion> notificaciones = this.notifService.buscarTodas();
        return notificaciones.stream().map(notif -> new NotificacionOutputDTO(notif.getId(),notif.getId_persona(),notif.getTitulo(),notif.getCuerpo(), notif.getFecha())).toList();
    }

    @GetMapping("/personas/{idPersona}")
    public List<NotificacionOutputDTO> buscar_notificaciones_por_persona(@Valid @PathVariable Long idPersona){
        List<Notificacion> notificaciones = this.notifService.buscar(idPersona);
        return notificaciones.stream().map(notif -> new NotificacionOutputDTO(notif.getId(),notif.getId_persona(),notif.getTitulo(),notif.getCuerpo(), notif.getFecha())).toList();
    }

}

package com.tp.donatrack.notificaciones.controllers;

import com.tp.donatrack.notificaciones.domain.entities.Notificacion;
import com.tp.donatrack.notificaciones.services.NotificacionService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/notificaciones")
public class NotificacionController {

    private final NotificacionService notificacionService;

    public NotificacionController(NotificacionService service){
        this.notificacionService = service;
    }

    @GetMapping("/")
    public List<Notificacion> buscarTodas() {
        return this.notificacionService.buscarTodas();
    }

    @GetMapping("/personas/{idPersona}")
    public List<Notificacion> buscarNotificacionesPorPersona(@PathVariable Long idPersona) {
        return this.notificacionService.buscar(idPersona);
    }
}

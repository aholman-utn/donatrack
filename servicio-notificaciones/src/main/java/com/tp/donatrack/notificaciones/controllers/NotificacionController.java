package com.tp.donatrack.notificaciones.controllers;

import com.tp.donatrack.notificaciones.dtos.NotificacionRequestDTO;
import com.tp.donatrack.notificaciones.services.NotificacionService;
import com.tp.donatrack.notificaciones.domain.notificacion.Notificacion;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notificaciones")
public class NotificacionController {
    private final NotificacionService service;

    public NotificacionController(NotificacionService service) {
        this.service = service;
    }

    @PostMapping()
    public ResponseEntity<Void> notificar(@Valid @RequestBody NotificacionRequestDTO dto) {
        Notificacion notif = new Notificacion(
            dto.getTitulo(), 
            dto.getCuerpo(), 
            dto.getAsunto()
        );

        this.service.notificar(notif, dto.getTipo(), dto.getContacto());
        return ResponseEntity.ok().build();
    }
}

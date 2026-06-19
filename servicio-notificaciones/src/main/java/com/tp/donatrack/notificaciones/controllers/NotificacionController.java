package com.tp.donatrack.notificaciones.controllers;

import com.tp.donatrack.notificaciones.domain.entities.Notificacion;
import com.tp.donatrack.notificaciones.dtos.NotificacionOutputDTO;
import com.tp.donatrack.notificaciones.dtos.NotificarServicioExterno;
import com.tp.donatrack.notificaciones.services.NotificacionService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.util.List;

@RestController
@RequestMapping("/api/notificaciones")
public class NotificacionController {

    private final NotificacionService notificacionService;
    private static final Logger logger = LoggerFactory.getLogger(NotificacionController.class);

    public NotificacionController(NotificacionService service){
        this.notificacionService = service;
    }

    @PostMapping("/notificar")
    public ResponseEntity<String> notificar(
        @Valid @RequestBody NotificarServicioExterno body
    ){
        try {
            this.notificacionService.notificar(body);
            logger.info("Notificación enviada exitosamente para la persona ID: {}", body.getIdPersona());

            return ResponseEntity.ok("Notificación procesada correctamente.");
        } catch (IllegalArgumentException e) {
            logger.error("Hubo un error al enviar notificación: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            logger.error("Hubo un error al enviar notificación para ID: {}", body.getIdPersona());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error interno al intentar enviar la notificación.");
        }
    }

    @GetMapping("/")
    public List<NotificacionOutputDTO> buscarTodas() {
        return this.notificacionService.buscarTodas().stream()
                .map(this::mapearADTO)
                .toList();
    }

    @GetMapping("/personas/{idPersona}")
    public List<NotificacionOutputDTO> buscarNotificacionesPorPersona(@PathVariable Long idPersona) {
        return this.notificacionService.buscar(idPersona).stream()
                .map(this::mapearADTO)
                .toList();
    }

    private NotificacionOutputDTO mapearADTO(Notificacion notif) {
        return new NotificacionOutputDTO(
                notif.getId(),
                notif.getId_persona(),
                notif.getAsunto(),
                notif.getMensaje(),
                notif.getDestinatario(),
                notif.getFecha()
        );
    }
}

package com.tp.donatrack.notificaciones.controllers;

import com.tp.commons.dtos.notificador.NotificacionRequestDTO;
import com.tp.donatrack.notificaciones.domain.entities.Notificacion;
import com.tp.donatrack.notificaciones.services.NotificacionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Comparator;
import java.time.LocalDateTime;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import java.util.Map;

@RestController
@RequestMapping("/api/notificaciones")
public class NotificacionController {

    private final NotificacionService notificacionService;

    public NotificacionController(NotificacionService service){
        this.notificacionService = service;
    }

    @PostMapping("/notificar")
    public ResponseEntity<?> notificar(@Valid @RequestBody NotificacionRequestDTO body) {
        try {
            this.notificacionService.notificar(body);
            return ResponseEntity.ok(Map.of(
                    "status", "OK",
                    "mensaje", "Notificación enviada exitosamente",
                    "medio", body.getMedio().name(),
                    "destinatario", body.getDestinatario()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "ERROR",
                    "mensaje", e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "status", "ERROR",
                    "mensaje", "Error interno al enviar notificación: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/me")
    public List<Aviso> avisosPropios(@AuthenticationPrincipal Jwt jwt) {
        Long id = Long.valueOf(jwt.getClaimAsString("donante_id"));
        return notificacionService.buscar(id).stream()
                .sorted(Comparator.comparing(Notificacion::getFecha, Comparator.nullsFirst(Comparator.naturalOrder()))
                        .reversed())
                .map(n -> new Aviso(n.getId(), n.getAsunto(), n.getMensaje(), n.getFecha()))
                .toList();
    }

    public record Aviso(Long id, String asunto, String mensaje, LocalDateTime fecha) {}

    @GetMapping("/")
    public List<Notificacion> buscarTodas() {
        return this.notificacionService.buscarTodas();
    }

    @GetMapping("/personas/{idPersona}")
    public List<Notificacion> buscarNotificacionesPorPersona(@PathVariable Long idPersona) {
        return this.notificacionService.buscar(idPersona);
    }
}

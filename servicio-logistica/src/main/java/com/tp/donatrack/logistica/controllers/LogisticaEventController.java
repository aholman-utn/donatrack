package com.tp.donatrack.logistica.controllers;

import com.tp.donatrack.logistica.domain.EventoLogistica;
import com.tp.donatrack.logistica.repository.LogisticaEventRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/logistica/rutas/eventos")
public class LogisticaEventController {

    private final LogisticaEventRepository eventRepository;

    public LogisticaEventController(LogisticaEventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @GetMapping
    public ResponseEntity<List<EventoLogistica>> obtenerEventos() {
        return ResponseEntity.ok(eventRepository.obtenerTodos());
    }
}

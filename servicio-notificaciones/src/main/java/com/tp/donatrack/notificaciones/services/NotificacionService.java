package com.tp.donatrack.notificaciones.services;

import com.tp.donatrack.notificaciones.domain.entities.MedioNotificador;
import com.tp.donatrack.notificaciones.domain.entities.Notificacion;
import com.tp.donatrack.notificaciones.domain.entities.iNotificador;
import com.tp.donatrack.notificaciones.dtos.NotificarServicioExterno;
import com.tp.donatrack.notificaciones.repositories.NotificacionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;

@Service
public class NotificacionService {
    private final NotificacionRepository notificacionRepository;
    private final List<iNotificador> notificadores;

    public NotificacionService(NotificacionRepository repo, List<iNotificador> notificadores) {
        this.notificacionRepository = repo;
        this.notificadores = notificadores != null ? notificadores : new ArrayList<>();
    }

    public void notificar(NotificarServicioExterno body) {
        iNotificador notificador = this.seleccionarNotificador(body.getMedio())
                .orElseThrow(() -> new IllegalArgumentException("No se encontró un notificador"));

        notificador.enviarNotificacion(body.getDestinatario(), body.getMensaje(), body.getAsunto());

        this.crearNotificacion(body.getIdPersona(), body.getAsunto(), body.getMensaje(), body.getDestinatario());
    }

    public List<Notificacion> buscar(Long id_persona) {
        return this.notificacionRepository.findByIdPersona(id_persona);
    }

    public List<Notificacion> buscarTodas() {
        return this.notificacionRepository.findAll();
    }

    private Optional<iNotificador> seleccionarNotificador(MedioNotificador medio) {
        return this.notificadores.stream()
                .filter(n -> n.getMedio().equals(medio))
                .findFirst();
    }

    private Notificacion crearNotificacion(Long idPersona, String asunto, String mensaje, String destinatario) {
        Notificacion nueva = new Notificacion();
        nueva.setId_persona(idPersona);
        nueva.setAsunto(asunto);
        nueva.setMensaje(mensaje);
        nueva.setDestinatario(destinatario);
        nueva.setFecha(LocalDateTime.now());

        return this.notificacionRepository.save(nueva);
    }
}
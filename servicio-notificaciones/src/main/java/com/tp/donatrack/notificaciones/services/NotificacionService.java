package com.tp.donatrack.notificaciones.services;

import com.tp.commons.domain.notificador.TipoNotificador;
import com.tp.commons.dtos.notificador.NotificacionRequestDTO;
import com.tp.donatrack.notificaciones.domain.entities.Notificacion;
import com.tp.donatrack.notificaciones.domain.entities.iNotificador;
import com.tp.donatrack.notificaciones.repositories.NotificacionRepository;
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

    public void notificar(NotificacionRequestDTO body) {
        Notificacion notificacion = new Notificacion();
        notificacion.setIdPersona(body.getIdPersona());
        notificacion.setAsunto(body.getAsunto());
        notificacion.setMensaje(body.getMensaje());
        notificacion.setDestinatario(body.getDestinatario());
        notificacion.setFecha(LocalDateTime.now());
        notificacion.setEnviado(false);

        try {
            iNotificador notificador = this.seleccionarNotificador(body.getMedio())
                    .orElseThrow(() -> new IllegalArgumentException("No se encontró un notificador"));

            notificacion.setMedio(notificador.getMedio());
            notificador.enviarNotificacion(body.getDestinatario(), body.getMensaje(), body.getAsunto());

            notificacion.setEnviado(true);

        } catch (Exception e) {
            System.err.println("Error al enviar notificación: " + e.getMessage());
        }
        this.guardarEnBaseDeDatos(notificacion);
    }

    public List<Notificacion> buscar(Long idPersona) {
        return this.notificacionRepository.findByIdPersona(idPersona);
    }

    public List<Notificacion> buscarTodas() {
        return this.notificacionRepository.findAll();
    }

    private Optional<iNotificador> seleccionarNotificador(TipoNotificador medio) {
        return this.notificadores.stream()
                .filter(n -> n.getMedio().equals(medio))
                .findFirst();
    }

    private Notificacion guardarEnBaseDeDatos(Notificacion notificacion) {
        return this.notificacionRepository.save(notificacion);
    }
}
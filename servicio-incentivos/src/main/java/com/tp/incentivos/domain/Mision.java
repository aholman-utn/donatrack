package com.tp.incentivos.domain;

import java.time.LocalDate;

import lombok.*;

@Getter
@Setter
public abstract class Mision {

    protected int progresoActual = 0;
    protected boolean completada = false;
    protected LocalDate fechaObtencion = null;
    protected Insignia insigniaAsociada;
    // protected final String titulo;
    // protected final String descripcion;

    public abstract int getObjetivo();

    public abstract String getTitulo();

    public abstract String getDescripcion();

    public abstract void actualizarProgreso(InfoDonacion infoDonacion);

    public boolean evaluarYMarcarCompletitud(String usuario, String nombreMision, String descripcion) {
        if (!completada && progresoActual >= getObjetivo()) {
            this.completada = true;
            this.fechaObtencion = LocalDate.now();

            postearInsigniaEnn8n(usuario, nombreMision, descripcion);
            // TODO notificacion en servicio de notificaciones
            return true;
        }
        return false;
    }

    private void postearInsigniaEnn8n(String usuario, String nombreMision, String descripcion) {
        try {
            org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();
            java.util.Map<String, String> body = new java.util.HashMap<>();
            body.put("user", usuario);
            body.put("nombreMision", nombreMision);
            body.put("descripcion", descripcion);
            restTemplate.postForEntity("http://localhost:5678/webhook-test/nueva_insignia", body, String.class);
        } catch (Exception e) {
            System.err.println("Error al enviar webhook: " + e.getMessage());
        }
    }
}

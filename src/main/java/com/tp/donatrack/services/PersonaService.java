package com.tp.donatrack.services;

import com.tp.donatrack.domain.persona.Persona;
import com.tp.donatrack.domain.notificacion.Notificacion;
import com.tp.donatrack.domain.contacto.Notificador;
import com.tp.donatrack.domain.ubicacion.Direccion;

import java.util.List;

public class PersonaService {

    public PersonaService() {
    }

    public void create(Persona persona) {
        // TODO: Lógica para persistir la persona
    }

    public List<Persona> getAll() {
        // TODO: Lógica para devolver todas las personas
        return null;
    }

    public Persona update(Persona persona) {
        // TODO: Lógica para actualizar una persona
        return persona;
    }

    public void delete(Persona persona) {
        // TODO: Lógica para eliminar la persona
    }

    public List<Notificacion> getNotificaciones(Persona persona) {
        // TODO: Retornar las notificaciones
        // return persona.getNotificaciones();
        return null;
    }

    public void addNotificacion(Persona persona, Notificacion notificacion) {
        // TODO: Lógica para asociarle una nueva notificación
    }

    public void updateDireccion(Persona persona, Direccion nuevaDireccion) {
        // TODO: Lógica para actualizar la dirección
    }

    public void addMedioDeContacto(Persona persona, Notificador medio) {
        // TODO: Lógica para agregar un medio de contacto a la lista
    }

    public void removeMedioDeContacto(Persona persona, Notificador medio) {
        // TODO: Lógica para quitar un medio de contacto
    }

    public void setMedioPredeterminado(Persona persona, Notificador medio) {
        // TODO: Lógica para definir el medio predeterminado
    }

}

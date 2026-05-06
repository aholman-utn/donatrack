package domain.contacto;

import domain.notificacion.Notificacion;

public interface MedioDeContacto {
    void enviarNotificacion(Notificacion notificacion);
}

package domain.contacto;

import lombok.Getter;
import lombok.Setter;

import domain.notificacion.Notificacion;

@Getter
@Setter
public class NotificadorEmail implements MedioDeContacto {
    private String correo;

    @Override
    public void enviarNotificacion(Notificacion notificacion) {
        // Implementación
    }
}

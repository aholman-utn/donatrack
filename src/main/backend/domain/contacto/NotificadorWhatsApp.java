package domain.contacto;

import lombok.Getter;
import lombok.Setter;

import domain.notificacion.Notificacion;

@Getter
@Setter
public class NotificadorWhatsApp implements MedioDeContacto {
    private String telefono;
    private String caracteristica;

    @Override
    public void enviarNotificacion(Notificacion notificacion) {
        // Implementación
    }
}

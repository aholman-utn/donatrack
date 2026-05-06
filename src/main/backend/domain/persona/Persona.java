package domain.persona;

import domain.ubicacion.Direccion;
import domain.notificacion.Notificacion;
import domain.contacto.MedioDeContacto;
import java.util.List;

public abstract class Persona {
    protected List<Notificacion> notificaciones;
    protected Direccion direccion;
    protected List<MedioDeContacto> mediosDeContacto;
    protected MedioDeContacto medioPredeterminado;
}

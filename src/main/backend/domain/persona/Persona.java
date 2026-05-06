package domain.persona;

import lombok.Getter;
import lombok.Setter;

import domain.ubicacion.Direccion;
import domain.notificacion.Notificacion;
import domain.contacto.MedioDeContacto;
import java.util.List;


@Getter
@Setter
public abstract class Persona {
    private List<Notificacion> notificaciones;
    private Direccion direccion;
    private List<MedioDeContacto> mediosDeContacto;
    private MedioDeContacto medioPredeterminado;

}

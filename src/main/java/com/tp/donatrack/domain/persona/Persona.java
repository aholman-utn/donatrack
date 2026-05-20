package com.tp.donatrack.domain.persona;

import com.tp.donatrack.domain.ubicacion.Direccion;
import com.tp.donatrack.domain.notificacion.Notificacion;

import lombok.Getter;
import lombok.Setter;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

@Getter
@Setter
public abstract class Persona {
    private List<Notificacion> notificaciones = new ArrayList<>();
    private Direccion direccion;
    private List< Map< String, String > > medioDeContacto = new ArrayList<>();
    private Map< String, String > medioPredeterminado;

    public void agregarNotificacion(Notificacion notificacion) {
        this.notificaciones.add(notificacion);
    }

}

package com.tp.donatrack.domain.persona;

import com.tp.donatrack.domain.ubicacion.Direccion;
import com.tp.donatrack.domain.notificacion.Notificacion;
import com.tp.donatrack.domain.contacto.MedioDeContacto;

import lombok.Getter;
import lombok.Setter;
import java.util.List;
import java.util.ArrayList;

@Getter
@Setter
public abstract class Persona {
    private List<Notificacion> notificaciones = new ArrayList<>();
    private Direccion direccion;
    private List<MedioDeContacto> mediosDeContacto = new ArrayList<>();
    private MedioDeContacto medioPredeterminado;

    public void agregarNotificacion(Notificacion notificacion) {
        this.notificaciones.add(notificacion);
    }

}

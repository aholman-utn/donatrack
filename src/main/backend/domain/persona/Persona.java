package com.tp.donatrack.domain.persona;

import com.tp.donatrack.domain.ubicacion.Direccion;
import com.tp.donatrack.domain.notificacion.Notificacion;
import com.tp.donatrack.domain.contacto.MedioDeContacto;
import java.util.List;

public abstract class Persona {
    protected List<Notificacion> notificaciones;
    protected Direccion direccion;
    protected List<MedioDeContacto> mediosDeContacto;
    protected MedioDeContacto medioPredeterminado;
}

package com.tp.donatrack.domain.persona;

import com.tp.donatrack.domain.ubicacion.Direccion;
import com.tp.donatrack.domain.notificacion.Notificacion;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.time.LocalDateTime;

@Getter
@Setter
public abstract class Persona {
    private List<Notificacion> notificaciones = new ArrayList<>();
    private Direccion direccion;
    private Map<String, List<String>> medioDeContacto = new HashMap<>();
    private Map<String, String> medioPredeterminado;
    private LocalDateTime fechaUltimaInteraccion;

    public void agregarNotificacion(Notificacion notificacion) {
        this.notificaciones.add(notificacion);
    }

    public Map<String, List<String>> agregarMedioDeContacto(String key, String value) {
        List<String> values = this.medioDeContacto.get(key);

        if (values == null) {
            values = new ArrayList<>();
        }
        values.add(value);
        this.medioDeContacto.put(key, values);
        return this.medioDeContacto;
    }
}

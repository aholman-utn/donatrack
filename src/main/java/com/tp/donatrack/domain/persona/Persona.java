package com.tp.donatrack.domain.persona;

import com.tp.donatrack.domain.ubicacion.Direccion;
import com.tp.donatrack.domain.notificacion.Notificacion;
import com.tp.donatrack.domain.contacto.Notificador;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class Persona {
    private List<Notificacion> notificaciones;
    private Direccion direccion;
    private Map<String, String> mediosDeContacto;
    private String medioPredeterminado;

    public Persona(){
        this.notificaciones = new ArrayList<>();
        this.mediosDeContacto = new HashMap<>();
    }

    public void agregarMedioDeContacto(String medio, String valor){
        this.mediosDeContacto.put(medio, valor);
    }
}

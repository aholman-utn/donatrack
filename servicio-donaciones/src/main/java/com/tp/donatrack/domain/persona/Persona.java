package com.tp.donatrack.domain.persona;

import com.tp.donatrack.domain.ubicacion.Direccion;
import com.tp.donatrack.domain.notificador.TipoNotificador;

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
    private Long id;
    private Direccion direccion;
    private Map<String, List<String>> medioDeContacto = new HashMap<>();
    private Map<String, String> medioPredeterminado;
    private LocalDateTime fechaUltimaInteraccion;

    public Map<String, List<String>> agregarMedioDeContacto(String key, String value) {
        List<String> values = this.medioDeContacto.get(key);

        if (values == null) {
            values = new ArrayList<>();
        }
        values.add(value);
        this.medioDeContacto.put(key, values);
        return this.medioDeContacto;
    }

    public TipoNotificador getTipoNotificadorPreferido() {
        if (medioPredeterminado == null || medioPredeterminado.isEmpty()) {
            return TipoNotificador.EMAIL;
        }

        if (medioPredeterminado.containsKey("medio")) {
            return TipoNotificador.valueOf(medioPredeterminado.get("medio").toUpperCase());
        }

        String clave = medioPredeterminado.keySet().iterator().next();
        return TipoNotificador.valueOf(clave.toUpperCase());
    }

    public String getContactoPredeterminado() {
        if (medioPredeterminado == null || medioPredeterminado.isEmpty()) {
            return null;
        }

        if (medioPredeterminado.containsKey("valor")) {
            return medioPredeterminado.get("valor");
        }

        return medioPredeterminado.values().iterator().next();
    }
}

package com.tp.donatrack.logistica.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.springframework.data.annotation.Transient;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Parada {
    @JsonIgnore
    @Transient
    private Ruta ruta;

    private Integer orden;
    private String direccion;

    @Builder.Default
    private List<String> enviosIds = new ArrayList<>();

    @JsonIgnore
    @Transient
    private List<Long> donacionesSegmentadasIds;

    public void agregarEnvio(Envio envio) {
        if (this.enviosIds == null) {
            this.enviosIds = new ArrayList<>();
        }
        if (envio.getId() == null) {
            throw new IllegalArgumentException("El envío debe estar persistido para agregarlo a una parada");
        }
        this.enviosIds.add(envio.getId());
    }
}

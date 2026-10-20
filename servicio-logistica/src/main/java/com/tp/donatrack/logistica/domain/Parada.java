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
    private List<Envio> envios = new ArrayList<>();

    private List<Long> donacionesSegmentadasIds;

    public List<Long> getDonacionesSegmentadasIds() {
        if (envios != null && !envios.isEmpty()) {
            return envios.stream().map(Envio::getDonacionSegmentadaId).toList();
        }
        return donacionesSegmentadasIds != null ? donacionesSegmentadasIds : new ArrayList<>();
    }

    public void agregarEnvio(Envio envio) {
        if (this.envios == null) {
            this.envios = new ArrayList<>();
        }
        this.envios.add(envio);
        envio.setParada(this);
    }
}

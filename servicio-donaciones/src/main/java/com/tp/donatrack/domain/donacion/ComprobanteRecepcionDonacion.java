package com.tp.donatrack.domain.donacion;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComprobanteRecepcionDonacion {

    private String id;

    private Long donacionSegmentadaId;
    private String nombreDonante;
    private String nombreEntidad;
    private LocalDateTime fechaEntrega;
    private String detallesLogistica;

    public void generarId() {
        if (this.id == null || this.id.isEmpty()) {
            this.id = UUID.randomUUID().toString();
        }
    }
}
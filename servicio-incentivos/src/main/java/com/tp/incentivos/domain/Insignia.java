package com.tp.incentivos.domain;

import java.util.Date;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Insignia {
    private String titulo;
    private String descripcion;
    private String nombreDonante;
    private Date fechaObtencion = new Date();
    // TODO: private String imagen; la genero desde n8n. ver despues como la podemos
    // agregar aca

    public Insignia(String nombreDonante, String titulo, String descripcion) {
        this.nombreDonante = nombreDonante;
        this.titulo = titulo;
        this.descripcion = descripcion;
    }
}

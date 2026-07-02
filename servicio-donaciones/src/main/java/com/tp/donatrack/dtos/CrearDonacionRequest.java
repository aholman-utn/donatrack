package com.tp.donatrack.dtos;

import com.tp.donatrack.domain.bien.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@Setter
public class CrearDonacionRequest {
    private Long donanteId;
    private String descripcion;
    private List<BienRequest> bienes = new ArrayList<>();

    public List<Bien> toDomainBienes() {
        List<Bien> domainBienes = new ArrayList<>();
        for (BienRequest br : bienes) {
            SubCategoria sub = new SubCategoria(
                br.getSubCategoria().getCategoria(),
                br.getSubCategoria().getDescripcion(),
                br.getSubCategoria().getUnidad()
            );

            if ("PERECEDERO".equalsIgnoreCase(br.getTipo())) {
                domainBienes.add(new BienPerecedero(
                    br.getNombre(),
                    br.getDescripcion(),
                    br.getFoto(),
                    sub,
                    br.getFechaVencimiento()
                ));
            } else if ("DURADERO".equalsIgnoreCase(br.getTipo())) {
                domainBienes.add(new BienDuradero(
                    br.getNombre(),
                    br.getDescripcion(),
                    br.getFoto(),
                    sub,
                    br.getEstadoBien()
                ));
            }
        }
        return domainBienes;
    }

    @Getter
    @Setter
    public static class BienRequest {
        private String tipo; // "PERECEDERO" or "DURADERO"
        private String nombre;
        private String descripcion;
        private String foto;
        private SubCategoriaRequest subCategoria;
        
        // For perishable
        private Date fechaVencimiento;

        // For durable
        private EstadoBien estadoBien;
    }

    @Getter
    @Setter
    public static class SubCategoriaRequest {
        private CategoriaBien categoria;
        private String descripcion;
        private com.tp.commons.enums.Unidad unidad;
    }
}

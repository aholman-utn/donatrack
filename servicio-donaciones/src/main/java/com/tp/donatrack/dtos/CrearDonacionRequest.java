package com.tp.donatrack.dtos;

import com.tp.commons.domain.donaciones.Unidad;
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
        if (bienes == null || bienes.isEmpty()) {
            throw new IllegalArgumentException("Agregá al menos un bien.");
        }
        for (BienRequest br : bienes) {
            if (br == null || br.getSubCategoria() == null) {
                throw new IllegalArgumentException("Cada bien debe tener una subcategoría.");
            }
            if (br.getCantidad() < 1 || br.getCantidad() > 10_000 - domainBienes.size()) {
                throw new IllegalArgumentException("La donación admite entre 1 y 10.000 bienes.");
            }
            var subCategoria = br.getSubCategoria();
            SubCategoria sub = new SubCategoria(
                subCategoria.getCategoria(),
                subCategoria.getDescripcion(),
                subCategoria.getUnidad()
            );

            boolean perecedero = "PERECEDERO".equalsIgnoreCase(br.getTipo());
            if (!perecedero && !"DURADERO".equalsIgnoreCase(br.getTipo())) {
                throw new IllegalArgumentException("El tipo de bien debe ser PERECEDERO o DURADERO.");
            }
            for (int i = 0; i < br.getCantidad(); i++) {
                Bien bien = perecedero
                    ? new BienPerecedero(br.getNombre(), br.getDescripcion(), br.getFoto(), sub, br.getFechaVencimiento())
                    : new BienDuradero(br.getNombre(), br.getDescripcion(), br.getFoto(), sub, br.getEstadoBien());
                domainBienes.add(bien);
            }
        }
        return domainBienes;
    }

    @Getter
    @Setter
    public static class BienRequest {
        private String tipo; // "PERECEDERO" or "DURADERO"
        private int cantidad = 1;
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
        private Unidad unidad;
    }
}

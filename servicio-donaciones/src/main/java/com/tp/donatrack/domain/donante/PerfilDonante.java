package com.tp.donatrack.domain.donante;

import com.tp.donatrack.domain.bien.CategoriaBien;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class PerfilDonante {
    @Builder.Default
    private boolean visibilidadInsignia = true;

    @Builder.Default
    private List<ItemDonacion> historialDonaciones = new ArrayList<>();

    @Builder.Default
    private CategoriaDonante categoriaDonante = CategoriaDonante.COLABORADOR;

    public PerfilDonante() {
        this.visibilidadInsignia = true;
        this.historialDonaciones = new ArrayList<>();
        this.categoriaDonante = CategoriaDonante.COLABORADOR;
    }

    public void registrarEntrega(Long entidadBeneficiariaId, CategoriaBien categoria) {
        if (this.historialDonaciones == null) {
            this.historialDonaciones = new ArrayList<>();
        }
        ItemDonacion item = ItemDonacion.builder()
                .fecha(LocalDate.now())
                .categoria(categoria)
                .build();
        this.historialDonaciones.add(item);
    }

    public void registrarCategoria(CategoriaBien categoria) {
        if (categoria != null) {
            registrarEntrega(null, categoria);
        }
    }

    public void subirCategoria() {
        if (this.historialDonaciones == null) {
            return;
        }
        int size = this.historialDonaciones.size();
        if (this.categoriaDonante == CategoriaDonante.COLABORADOR && size >= 5) {
            this.categoriaDonante = CategoriaDonante.SOSTENEDOR;
        } else if (this.categoriaDonante == CategoriaDonante.SOSTENEDOR && size >= 15) {
            this.categoriaDonante = CategoriaDonante.TRANSFORMADOR;
        }
    }
}
